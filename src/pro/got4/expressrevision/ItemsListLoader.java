package pro.got4.expressrevision;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pro.got4.expressrevision.dialogs.ProgressDialogFragment;
import pro.got4.expressrevision.dialogs.ProgressDialogFragment.DialogListener;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import android.widget.Toast;

/**
 * ����������� ���������, ��������� ��������-������.
 * 
 * @author programmer
 * 
 */
public class ItemsListLoader extends FragmentActivity implements
		LoaderCallbacks<Void>, DialogListener {

	private static final int DEMO_MODE_SLEEP_TIME = 50;

	public static final int ITEMSLISTLOADER_ID = 1;
	public static final String ITEMSLIST_LOADER_TAG = "itemslistloaderfragmentactivity_tag";

	public static final String NUMBER_OF_TREADS_FIELD_NAME = "itemsListLoadedNumberOfUsingThreads";

	private static final String FIELD_DOC_LOADED_ROWS_TOTAL_NAME = "docRowsTotal";
	private static final String FIELD_DOC_ROWS_TOTAL_NAME = "docLoadedRowsTotal";

	private static final String FIELD_DOC_NUM_NAME = "docNum";
	private static final String FIELD_DOC_DATE_NAME = "docDate";
	private static final String FIELD_CONNECTION_STRING_NAME = "connectionString";

	private static final String FIELD_FIRST_LOADED_ROW_NUM_NAME = "firstRow";
	private static final String FIELD_LAST_LOADED_ROW_NUM_NAME = "lastRow";
	private static final String FIELD_ROWS_IN_EACH_PARCEL_NAME = "rowsInDataParcel";

	public static final int BUTTON_BACK_ID = 1;

	// ��� ����, ������� � ������� �������� ������ ����������.
	public static final String CONNECTION_STRING_FIELD_NAME = "connectionStringItems";

	// ���� XML-�������.
	private static final String DOC_TAG_NAME = "doc";
	private static final String DOC_NUM_TAG_NAME = "num";
	private static final String DOC_DATE_TAG_NAME = "date";

	private static final String ROW_TAG_NAME = "doc_row";
	private static final String ROW_NUM_TAG_NAME = "num";

	private static final String ITEM_TAG_NAME = "item";
	private static final String ITEM_CODE_TAG_NAME = "code";
	private static final String ITEM_DESCR_TAG_NAME = "descr";
	private static final String ITEM_DESCR_FULL_TAG_NAME = "descr_full";
	private static final String ITEM_USE_SPECIF_TAG_NAME = "use_specif";

	private static final String SPECIF_TAG_NAME = "specif";
	private static final String SPECIF_CODE_TAG_NAME = "code";
	private static final String SPECIF_DESCR_TAG_NAME = "descr";

	private static final String MEASUR_TAG_NAME = "measur";
	private static final String MEASUR_DESCR_TAG_NAME = "descr";

	private static final String PRICE_TAG_NAME = "price";
	private static final String QUANT_ACC_TAG_NAME = "quant_acc";
	private static final String QUANT_TAG_NAME = "quant";

	// ��������� ���� ��� �������������� � ������, ������������ ��� ������������
	// �������������� ���������.
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	// �� ��������� ������ (��. ���������, ������� ����������, ���� �����
	// �������� �� ������ �����������).
	private static WeakReference<ItemsListLoader> WEAK_REF_ACTIVITY;

	// //////////////////////////////////////////////////////////////////////
	// ������������� ����, �������� ������� �������� �������������� ���
	// �������������� ����������.
	//
	private ProgressDialogFragment pDialog; // ������������ � ��������.

	private ProgressHandler progressHandler; // ������������ � AsyncTaskLoader.

	private String connectionString;
	private Long docDate = Long.valueOf(0);
	private String docNum = "";
	private int docRowsTotal = 0;

	private int docLoadedRowsTotal = 0; // ���������� ����������� ����� �� ����
										// �����������.

	private int threadsNumber = 0;
	private int maxRowsInParcel = 0;

	// /////////////////////////////////////////////////////
	// �������.
	// /////////////////////////////////////////////////////

	/**
	 * �������, ����������� ��������� � ������� ������� ��������.
	 */
	private static class ProgressHandler extends Handler {

		private WeakReference<ItemsListLoader> wrActivity;

		public ProgressHandler(WeakReference<ItemsListLoader> wrActivity) {

			this.wrActivity = wrActivity;
		}

		/**
		 * ��������� ����������, ��� UI ������� ������� ��������� ��������.
		 * 
		 * @param wrActivity
		 */
		public void setActivity(WeakReference<ItemsListLoader> wrActivity) {

			this.wrActivity = wrActivity;
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			// ��������� �������� ���������.
			wrActivity.get().pDialog.setIndeterminate(msg.arg2 == 0 ? false
					: true);
			wrActivity.get().pDialog.setIncrementMode(true);
			wrActivity.get().pDialog.setMax(msg.arg1);
			wrActivity.get().pDialog.setProgress(msg.what);

			return;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this,
					getString(R.string.itemsConnectionParametersNotSet),
					Toast.LENGTH_LONG).show();
			finish();
		} else {

			connectionString = extras.getString(CONNECTION_STRING_FIELD_NAME);
			if (!Main.isDemoMode()
					&& (connectionString == null || connectionString.isEmpty())) {
				Toast.makeText(this,
						getString(R.string.itemsConnectionStringNotSet),
						Toast.LENGTH_LONG).show();
				finish();
			}

			docNum = extras.getString(DBase.FIELD_DOC_NUM_NAME);

			String docDateString = extras.getString(DBase.FIELD_DOC_DATE_NAME);
			docDate = Long.valueOf(docDateString);

			docRowsTotal = extras.getInt(DBase.FIELD_DOC_ROWS_NAME);
		}

		WEAK_REF_ACTIVITY = new WeakReference<ItemsListLoader>(this);

		// ����� �������, ����� ���������� ��� ������ ����������.
		pDialog = (ProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(ITEMSLIST_LOADER_TAG);

		// ���� ������ ��� �� ����������, �� ��� ����� �������.
		if (pDialog == null) {

			pDialog = new ProgressDialogFragment();
			pDialog.setTitle(getString(R.string.loadingItems));
			pDialog.setMessage(getString(R.string.pleaseWait));

			pDialog.show(getSupportFragmentManager(), ITEMSLIST_LOADER_TAG);
		}

		if (savedInstanceState != null) {
			docLoadedRowsTotal = savedInstanceState.getInt(
					FIELD_DOC_ROWS_TOTAL_NAME, 0);
		}

		if (Main.isDemoMode()) {

			// � ����-������ ���������� ������ ������.
			threadsNumber = 1;

		} else {

			threadsNumber = Integer.valueOf(PreferenceManager
					.getDefaultSharedPreferences(this).getString(
							NUMBER_OF_TREADS_FIELD_NAME, "1"));
		}

		// ������������ ���������� ����� � ����������� ������.
		maxRowsInParcel = Integer.valueOf(PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						FIELD_ROWS_IN_EACH_PARCEL_NAME, "100"));

		SparseArray<int[]> spArr = sliceInterval(docRowsTotal, threadsNumber,
				maxRowsInParcel);
		for (int i = 0; i < spArr.size(); i++) {

			// ������ ��������� �������� ����� ����������:
			// - ����� ������ ������ ���������, ������� �� ������ ���������.
			// - ����� ��������� ������ ���������, ������� �� ������ ���������.
			// - ����������� ���������� ����� � ����� ����������� ������.
			Bundle loaderArgs = new Bundle();
			loaderArgs.putString(FIELD_DOC_NUM_NAME, docNum);
			loaderArgs.putLong(FIELD_DOC_DATE_NAME, docDate);
			loaderArgs
					.putString(FIELD_CONNECTION_STRING_NAME, connectionString);

			loaderArgs.putInt(FIELD_FIRST_LOADED_ROW_NUM_NAME,
					spArr.get(i)[0] + 1);
			loaderArgs.putInt(FIELD_LAST_LOADED_ROW_NUM_NAME,
					spArr.get(i)[1] + 1);
			loaderArgs.putInt(FIELD_ROWS_IN_EACH_PARCEL_NAME, maxRowsInParcel);

			getSupportLoaderManager().initLoader(i, loaderArgs, this);
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		// ������������ �������� ��������-�������.
		if (progressHandler == null) {
			progressHandler = new ProgressHandler(WEAK_REF_ACTIVITY);
		} else {
			progressHandler.setActivity(WEAK_REF_ACTIVITY);
		}
	}

	@Override
	public void onPause() {

		super.onPause();

		progressHandler = null;

		// ���� ��������� �� ��� ������, �� ��������� 0.
		// ���� ��, �� 1.
		if (docRowsTotal == docLoadedRowsTotal) {
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		outState.putInt(FIELD_DOC_LOADED_ROWS_TOTAL_NAME, docLoadedRowsTotal);
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle args) {

		Loader<Void> ldr = new ItemsAsyncTaskLoader(this, args);

		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void data) {

		setResult(RESULT_OK);
		onCloseDialog(ITEMSLISTLOADER_ID, BUTTON_BACK_ID);

	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {

		setResult(RESULT_CANCELED);
		onCloseDialog(ITEMSLISTLOADER_ID, BUTTON_BACK_ID);

	}

	// /////////////////////////////////////////////////////
	// AsyncTaskLoader<Void>
	// /////////////////////////////////////////////////////
	private static class ItemsAsyncTaskLoader extends AsyncTaskLoader<Void> {

		private Context context;

		private MediaPlayer mp;

		private DBase dBase;

		private String docNum;
		private Long docDate;
		private String connectionString;

		// ��������� ����� � ���������.
		SparseArray<int[]> spArr;

		public ItemsAsyncTaskLoader(Context context, Bundle args) {

			super(context);

			if (args != null) {

				docNum = args.getString(FIELD_DOC_NUM_NAME);
				docDate = args.getLong(FIELD_DOC_DATE_NAME);
				connectionString = args.getString(FIELD_CONNECTION_STRING_NAME);

				int firstLoadersRowNumber = args
						.getInt(FIELD_FIRST_LOADED_ROW_NUM_NAME);
				int lastLoadersRowNumber = args
						.getInt(FIELD_LAST_LOADED_ROW_NUM_NAME);
				int maxRowsInParcel = args
						.getInt(FIELD_ROWS_IN_EACH_PARCEL_NAME);

				// ���������� ����� � ��������� � ������ ����������.
				int rowsTotal = lastLoadersRowNumber - firstLoadersRowNumber
						+ 1;

				// ���������� ��������, ����������� ��� ��������� ����������
				// ���������� ����� (� ����������� �����).
				int queries = (int) Math.ceil((double) rowsTotal
						/ maxRowsInParcel);

				// ������ �� ����������� ��������� ����� � ���������� ����� �
				// ������ ������������ �������� ����� �� �������.
				spArr = sliceInterval(rowsTotal, queries, maxRowsInParcel);
			}

			this.context = context;
		}

		@Override
		public void onStartLoading() {

			dBase = new DBase(context);
			dBase.open();

			mp = MediaPlayer.create(context, R.raw.powerup);
			mp.setLooping(false);

			forceLoad();

			super.onStartLoading();
		}

		@Override
		public Void loadInBackground() {

			if (Main.isDemoMode()) {

				String docDateString = dateFormatter.format(docDate);
				String docId = docDateString.concat(docNum);
				Cursor cursor = dBase.getRowsFiltered(
						DBase.TABLE_ITEMS_DEMO_NAME, DBase.FIELD_DOC_ID_NAME
								+ " = ?", docId, DBase.FIELD_ROW_NUM_NAME);

				cursor.moveToFirst();
				if (cursor.isFirst()) {

					if (!isStarted() || isAbandoned() || isReset())
						return null;

					dBase.copyItemRow(DBase.TABLE_ITEMS_NAME, cursor);

					int rowsLoaded = 1;

					WEAK_REF_ACTIVITY.get().incrementDocLoadedRowsTotal(
							rowsLoaded);

					setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(),
							rowsLoaded, WEAK_REF_ACTIVITY.get()
									.getDocRowsTotal(), 0);

					while (cursor.moveToNext()) {

						try {
							TimeUnit.MILLISECONDS.sleep(DEMO_MODE_SLEEP_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (!isStarted() || isAbandoned() || isReset())
							break;

						dBase.copyItemRow(DBase.TABLE_ITEMS_NAME, cursor);

						rowsLoaded = 1;

						WEAK_REF_ACTIVITY.get().incrementDocLoadedRowsTotal(
								rowsLoaded);

						setProgress(WEAK_REF_ACTIVITY.get()
								.getProgressHandler(), rowsLoaded,
								WEAK_REF_ACTIVITY.get().getDocRowsTotal(), 0);
					}
				}

			} else {

				// �������� � �������.
				// �������� ������ ������ ����� ���:
				// ���� ������ ����������� �����:
				// http://express.nsk.ru:9999/eritems.php?deviceid=12345
				// &docdate=20150105162307&docnum=%27%D0%AD%D0%9A%D0%A100000008%27
				// &firstrow=1&lastrow=10
				// ���� �� ������ ����������� �����:
				// http://express.nsk.ru:9999/eritems.php?deviceid=12345&docdate=20141223120310&docnum="���00000001"

				// ��������� �������������� ����������.
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String deviceId = tm.getDeviceId();

				// ���� � �� ����� ��� ����������� � ������ �����-�����,
				// � � ������� ������������� � ���� "20140101000000".
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						"yyyyMMddHHmmss", Locale.getDefault());
				String dateURIFormattedString = dateFormatter.format(docDate);
				String query = "?deviceid=" + deviceId + "&docdate="
						+ dateURIFormattedString + "&docnum='";

				// � ������ ��������� ����� ����������� ���������, �������
				// ���������� ��������������.
				try {
					query = query + URLEncoder.encode(docNum, HTTP.UTF_8) + "'";
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();

				try {

					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

					// ������� ���������� � ���������� ������� �� ������� ��
					// ���.
					for (int i = 0; i < spArr.size(); i++) {

						String queryRowsInterval = "&firstrow="
								+ (spArr.get(i)[0] + 1) + "&lastrow="
								+ (spArr.get(i)[1] + 1);

						String uriString = connectionString + query
								+ queryRowsInterval;

						// ��������������� ������ �������� ����� ����.
						Document domDoc = dBuilder.parse(uriString);
						domDoc.getDocumentElement().normalize();

						// �������.
						// ��������� ���� ����� ����������. �.�. �� ��� ������
						// ����������� ���������� ������ ������ ���������, �� �
						// ���� ������ ����.
						parseDocument(domDoc, dBase);
					}

				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} // if (Main.isDemoMode()) {

			mp.start();

			return null;
		}

		@Override
		public void onStopLoading() {

			cancelLoad();

			super.onStopLoading();

			onReleaseResources();
		}

		@Override
		public void onCanceled(Void data) {

			super.onCanceled(data);

			onReleaseResources();
		}

		@Override
		protected void onReset() {

			super.onReset();

			stopLoading();

			onReleaseResources();
		}

		@Override
		public void onAbandon() {

			super.onAbandon();
		}

		/**
		 * ������������ ��������.
		 */
		private void onReleaseResources() {
			if (mp != null)
				mp.release();
		}

		/**
		 * ��������� ��������� ����������.
		 */
		private void setProgress(ProgressHandler progressHandler,
				int progressIncrement, int max, int indeterminate) {

			// ��������� ��������� ����������.
			if (progressHandler != null) {

				android.os.Message msg = new android.os.Message();
				msg.what = progressIncrement;
				msg.arg1 = max;
				msg.arg2 = indeterminate;

				progressHandler.sendMessage(msg);
			}
		}

		/**
		 * ������� DOM-���������, ����������� XML � ���������� ��� ������� � ��.
		 * 
		 * @return ���������� ���������� �����.
		 */
		private int parseDocument(Document domDoc, DBase dBase) {

			// ���������� �����, ����������� � ������ ������.
			int rowsCounter = 0;

			NodeList docNodes = domDoc.getElementsByTagName(DOC_TAG_NAME);

			// ���������� ������ � ���� ���������.
			String docNumValue = "";
			String docDateTimeValue = "";

			int docsTotal = docNodes.getLength();
			if (docsTotal > 0) {

				Node docNode = docNodes.item(0);

				NamedNodeMap docAttr = docNode.getAttributes();
				Node docNumNode = docAttr.getNamedItem(DOC_NUM_TAG_NAME);
				Node docDateTimeNode = docAttr.getNamedItem(DOC_DATE_TAG_NAME);

				if (docNumNode != null)
					docNumValue = docNumNode.getTextContent();

				if (docDateTimeNode != null)
					docDateTimeValue = docDateTimeNode.getTextContent();
			}

			// ��������� ���� ����� �����.
			NodeList rowNodes = domDoc.getElementsByTagName(ROW_TAG_NAME);
			int rows = rowNodes.getLength();

			// ������� ���� �����.
			for (int rowIdx = 0; rowIdx < rows; rowIdx++) {

				if (!isStarted() || isAbandoned() || isReset())
					return rowsCounter;

				Node rowNode = rowNodes.item(rowIdx);

				// ���������� ������ ������.
				NamedNodeMap rowAttr = rowNode.getAttributes();
				Node rowNumNode = rowAttr.getNamedItem(ROW_NUM_TAG_NAME);

				String rowNumValue = rowNumNode.getTextContent();

				// ���������� ����������� ������.
				String itemCode = ""; // ��� ������������.
				String itemDescr = ""; // ������������ ������������.
				String itemDescrFull = ""; // ������ ������������
											// ������������.
				int itemUseSpecif = 0; // ����� ���� ��
										// ���������������.
				String specifCode = ""; // ��� ��������������
										// ������������.
				String specifDescr = ""; // ������������
											// ��������������
											// ������������.
				String measurDescr = ""; // ������������ �������
											// ���������.
				float price = 0; // ����.
				float quantAcc = 0; // ���������� � �����.
				float quant = 0; // ���������� �� ��������������.

				// ������� ���� ����� ������.
				NodeList rowFields = rowNode.getChildNodes();
				int rowFieldsTotal = rowFields.getLength();
				for (int rowFieldIdx = 0; rowFieldIdx < rowFieldsTotal; rowFieldIdx++) {

					Node rowFieldNode = rowFields.item(rowFieldIdx);
					String rowFieldName = rowFieldNode.getNodeName();

					if (rowFieldName.equals(ITEM_TAG_NAME)) {

						// ��������� ���� ������������.
						NamedNodeMap itemAttr = rowFieldNode.getAttributes();

						Node itemCodeNode = itemAttr
								.getNamedItem(ITEM_CODE_TAG_NAME);
						itemCode = itemCodeNode.getTextContent();

						// ������� ���� ����� ������������.
						NodeList itemFields = rowFieldNode.getChildNodes();
						int itemFieldsTotal = itemFields.getLength();
						for (int itemFieldIdx = 0; itemFieldIdx < itemFieldsTotal; itemFieldIdx++) {

							Node itemFieldNode = itemFields.item(itemFieldIdx);
							String itemFieldName = itemFieldNode.getNodeName();

							if (itemFieldName.equals(ITEM_DESCR_TAG_NAME)) {
								itemDescr = itemFieldNode.getTextContent();

							} else if (itemFieldName
									.equals(ITEM_DESCR_FULL_TAG_NAME)) {
								itemDescrFull = itemFieldNode.getTextContent();
							} else if (itemFieldName
									.equals(ITEM_USE_SPECIF_TAG_NAME)) {

								String itemUseSpecifSting = itemFieldNode
										.getTextContent();

								if (itemUseSpecifSting.equals("1")) {
									itemUseSpecif = 1;
								} else {
									itemUseSpecif = 0;
								}
							}
						}

					} else if (rowFieldName.equals(SPECIF_TAG_NAME)) {

						// ��������� ���� ��������������
						// ������������.
						NamedNodeMap specifAttr = rowFieldNode.getAttributes();

						Node specifCodeNode = specifAttr
								.getNamedItem(SPECIF_CODE_TAG_NAME);
						specifCode = specifCodeNode.getTextContent();

						// ������� ���� ����� ��������������
						// ������������.
						NodeList specifFields = rowFieldNode.getChildNodes();
						int specifFieldsTotal = specifFields.getLength();
						for (int specifFieldIdx = 0; specifFieldIdx < specifFieldsTotal; specifFieldIdx++) {

							Node specifFieldNode = specifFields
									.item(specifFieldIdx);
							String specifFieldName = specifFieldNode
									.getNodeName();

							if (specifFieldName.equals(SPECIF_DESCR_TAG_NAME)) {
								specifDescr = specifFieldNode.getTextContent();
							}
						}

					} else if (rowFieldName.equals(MEASUR_TAG_NAME)) {

						// ������� ���� ����� ������� ���������.
						NodeList measurFields = rowFieldNode.getChildNodes();
						int measurFieldsTotal = measurFields.getLength();
						for (int measurFieldIdx = 0; measurFieldIdx < measurFieldsTotal; measurFieldIdx++) {

							Node measurFieldNode = measurFields
									.item(measurFieldIdx);
							String measurFieldName = measurFieldNode
									.getNodeName();

							if (measurFieldName.equals(MEASUR_DESCR_TAG_NAME)) {
								measurDescr = measurFieldNode.getTextContent();
							}
						}

					} else if (rowFieldName.equals(PRICE_TAG_NAME)) {

						String priceString = rowFieldNode.getTextContent();
						price = Float.parseFloat(priceString.replace(",", "."));

					} else if (rowFieldName.equals(QUANT_ACC_TAG_NAME)) {

						String quant_accString = rowFieldNode.getTextContent();
						quantAcc = Float.parseFloat(quant_accString.replace(
								",", "."));

					} else if (rowFieldName.equals(QUANT_TAG_NAME)) {

						String quantString = rowFieldNode.getTextContent();
						quant = Float.parseFloat(quantString.replace(",", "."));
					}
				}

				ContentValues itemValues = new ContentValues();

				// ������������� ��������� ������� �� ���� ���������
				// � ��� ������.
				itemValues.put(DBase.FIELD_DOC_ID_NAME,
						DBase.getDocId(docNumValue, docDateTimeValue));
				itemValues.put(DBase.FIELD_ROW_NUM_NAME,
						Integer.parseInt(rowNumValue));
				itemValues.put(DBase.FIELD_ITEM_CODE_NAME, itemCode);
				itemValues.put(DBase.FIELD_ITEM_DESCR_NAME, itemDescr);
				itemValues.put(DBase.FIELD_ITEM_DESCR_FULL_NAME, itemDescrFull);
				itemValues.put(DBase.FIELD_ITEM_USE_SPECIF_NAME, itemUseSpecif);
				itemValues.put(DBase.FIELD_SPECIF_CODE_NAME, specifCode);
				itemValues.put(DBase.FIELD_SPECIF_DESCR_NAME, specifDescr);
				itemValues.put(DBase.FIELD_MEASUR_DESCR_NAME, measurDescr);
				itemValues.put(DBase.FIELD_PRICE_NAME, price);
				itemValues.put(DBase.FIELD_QUANT_ACC_NAME, quantAcc);
				itemValues.put(DBase.FIELD_QUANT_NAME, quant);

				dBase.insert(dBase.getSQLiteDatabase(), DBase.TABLE_ITEMS_NAME,
						itemValues);

				++rowsCounter;

				WEAK_REF_ACTIVITY.get().incrementDocLoadedRowsTotal(1);

				setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(), 1,
						WEAK_REF_ACTIVITY.get().getDocRowsTotal(), 0);
			}

			return rowsCounter;
		}
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();

		onCloseDialog(ITEMSLISTLOADER_ID, BUTTON_BACK_ID);
	}

	/**
	 * ���������� ������ �� ������� �������.
	 * 
	 * @return
	 */
	public ProgressHandler getProgressHandler() {

		return progressHandler;
	}

	// ��������� ��������-�������.
	public void onCloseDialog(int dialogId, int buttonId) {

		// ��������������� ��� ����������.
		for (int i = 0; i < threadsNumber; i++) {
			getSupportLoaderManager().getLoader(i).abandon();
		}

		finish();
	}

	/**
	 * @param rowsCounter
	 *            the rowsCounter to set
	 */
	public void setDocRowsTotal(int rows) {
		docRowsTotal = rows;
	}

	public int getDocRowsTotal() {
		return docRowsTotal;
	}

	/**
	 * @param rowsCounter
	 *            the rowsCounter to set
	 */
	public void incrementDocLoadedRowsTotal(int increment) {
		docLoadedRowsTotal += increment;
	}

	/**
	 * @param rowsTotal
	 *            the rowsTotal to set
	 */
	public void setDocLoadedRowsTotal(int rows) {
		docLoadedRowsTotal = rows;
	}

	public int getDocLoadedRowsTotal() {
		return docLoadedRowsTotal;
	}

	/**
	 * ������������� �������� �� ������������, �������� � ������ �����������
	 * ���������� ���������� ��������� � ������ ���������.
	 * 
	 * @param itemsTotal
	 *            ���������� ���������
	 * @param numberOfIntervals
	 *            ���������� �������������, � ������� �.�. ������������ ��������
	 * @param itemsPerInterval
	 *            �������� ���������� ��������� � ���������. ���� ==0, ��
	 *            �������� ����� ���������� ������������ �� �������������.
	 * @return SparseArray, ������ ������� �������� �������� ������ �� ����
	 *         ���������.<br>
	 *         ������ - ��� ������ ������� �������� ������������;<br>
	 *         ������ - ��� ������ ���������� �������� ������������.
	 */
	private static SparseArray<int[]> sliceInterval(int itemsTotal,
			int numberOfIntervals, int itemsPerInterval) {

		SparseArray<int[]> spArray = new SparseArray<int[]>();

		int lastIntervalItemIdx = -1;

		for (int i = 0; i < numberOfIntervals; i++) {

			// ���������� ��������:
			int intervalsLeft = numberOfIntervals - i;

			// ���������������� ��������� ��������:
			int itemsLeft = itemsTotal - (lastIntervalItemIdx + 1);

			// ���� ���������������� ����� ������ ���, �� ������ ������������
			// ��������� �� �����.
			if (itemsLeft <= 0)
				break;

			// ������ ������� �������� ��������� ���������� ������������.
			int firstIntervalItemIdx = lastIntervalItemIdx + 1;

			// ���������, ������������ �� ������ �� ���������� ����������
			// (� ����������� �� ��������):
			int itemsLeftPerInterval = (int) Math.ceil((double) itemsLeft
					/ intervalsLeft);

			// ���� ���������� ���������, ������������ �� �������� ���������
			// ������ ����������� ���������� ��������� � ������, �� �� ��������
			// ������ ����������� ������ ���������� ����������.
			// ��� ����� ��� ����, ����� ��������� ��������� �����������
			// ���������� ���������� ���������, � �� ������ �����������.
			// � ��������� ������, ���� ���������� �����, �� ��� ����� �������,
			// ��� �.�. ������������.
			if (itemsPerInterval != 0
					&& itemsLeftPerInterval < itemsPerInterval)
				itemsLeftPerInterval = itemsPerInterval;

			// ������ ���������� �������� ��������� ���������� ������������.
			lastIntervalItemIdx = firstIntervalItemIdx + itemsLeftPerInterval
					- 1;

			// ��������� ������� �� ����� �������� �� ������� ����� ����������
			// ���������.
			if (lastIntervalItemIdx > itemsTotal - 1)
				lastIntervalItemIdx = itemsTotal - 1;

			int[] arr = { firstIntervalItemIdx, lastIntervalItemIdx };

			spArray.put(i, arr);
		}

		return spArray;
	}
}
