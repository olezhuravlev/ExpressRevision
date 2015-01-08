package pro.got4.expressrevision;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pro.got4.expressrevision.dialogs.ProgressDialogFragment;
import pro.got4.expressrevision.dialogs.ProgressDialogFragment.DialogListener;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * ����������� ���������, ��������� ��������-������.
 * 
 * @author programmer
 * 
 */
public class DocsListLoader extends FragmentActivity implements
		LoaderCallbacks<Void>, DialogListener {

	public static final int DOCSLIST_LOADER_ID = 1;
	public static final String DOCSLIST_LOADER_TAG = "docslistloaderfragmentactivity_tag";

	public static final int BUTTON_BACK_ID = 1;

	private ProgressDialogFragment pDialog; // ������������ � ��������.

	private ProgressHandler progressHandler; // ������������ � AsyncTaskLoader.
	private static final int DEMO_MODE_SLEEP_TIME = 20;

	// ��� ����, ������� � ������� ������ ������ ����������.
	public static final String CONNECTION_STRING_FIELD_NAME = "connection_string";

	// ���� XML-�������.
	private static final String DOC_TAG_NAME = "doc";
	private static final String DOC_NUM_TAG_NAME = "num";
	private static final String DOC_DATE_TAG_NAME = "date";
	private static final String DOC_ROWS_TAG_NAME = "rows";

	private static final String STORE_TAG_NAME = "store";
	private static final String STORE_CODE_TAG_NAME = "code";
	private static final String STORE_DESCR_TAG_NAME = "descr";

	private static final String COMMENT_TAG_NAME = "comm";

	private static String connectionString;

	// ��������� ���� ��� �������������� �� �������� �������.
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	// �� ��������� ������ (��. ���������, ������� ����������, ���� �����
	// �������� �� ������ �����������).
	private static WeakReference<DocsListLoader> WEAK_REF_ACTIVITY;

	// /////////////////////////////////////////////////////
	// �������.
	// /////////////////////////////////////////////////////

	/**
	 * �������, ����������� ��������� � ������� ������� ��������.
	 */
	private static class ProgressHandler extends Handler {

		private WeakReference<DocsListLoader> wrActivity;

		public ProgressHandler(WeakReference<DocsListLoader> wrActivity) {

			Message.show("[hashCode = " + this.hashCode() + "], wrActivity = "
					+ wrActivity);

			this.wrActivity = wrActivity;
		}

		/**
		 * ��������� ����������, ��� UI ������� ������� ��������� ��������.
		 * 
		 * @param wrActivity
		 */
		public void setActivity(WeakReference<DocsListLoader> wrActivity) {

			Message.show(this);

			this.wrActivity = wrActivity;
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			// Message.show("[hashCode = " + this.hashCode() + "], what = ["
			// + msg.what + "]");

			// ��������� �������� ���������.
			wrActivity.get().pDialog.setProgress(msg.what);
			wrActivity.get().pDialog.setMax(msg.arg1);

			return;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Message.show(this);

		if (!Main.isDemoMode() && !Main.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
			finish();
		}

		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this, "��� ����� � ���������� ����������!",
					Toast.LENGTH_LONG).show();
			finish();
		}

		connectionString = extras.getString(CONNECTION_STRING_FIELD_NAME);
		if (connectionString == null) {
			Toast.makeText(this,
					"�� ������� ������ ���������� � ���������� ����������!",
					Toast.LENGTH_LONG).show();
			finish();
		}

		WEAK_REF_ACTIVITY = new WeakReference<DocsListLoader>(this);

		// ����� �������, ����� ���������� ��� ������ ����������.
		pDialog = (ProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(DOCSLIST_LOADER_TAG);

		// ���� ������ ��� �� ����������, �� ��� ����� �������.
		if (pDialog == null) {

			pDialog = new ProgressDialogFragment();
			pDialog.setTitle(getString(R.string.loadingDocuments));
			pDialog.setMessage(getString(R.string.pleaseWait));

			pDialog.show(getSupportFragmentManager(), DOCSLIST_LOADER_TAG);
		}

		getSupportLoaderManager().initLoader(DOCSLIST_LOADER_ID, null, this);
	}

	@Override
	public void onResume() {

		Message.show("[hashCode = " + this.hashCode()
				+ "], (before) progressHandler = " + progressHandler);

		// ������������ �������� ��������-�������.
		if (progressHandler == null) {
			progressHandler = new ProgressHandler(WEAK_REF_ACTIVITY);
		} else {
			progressHandler.setActivity(WEAK_REF_ACTIVITY);
		}

		Message.show("[hashCode = " + this.hashCode()
				+ "], (after) progressHandler = " + progressHandler);

		super.onResume();
	}

	@Override
	public void onPause() {

		Message.show(this);

		progressHandler = null;

		super.onPause();
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle bndl) {

		Message.show("[hashCode = " + this.hashCode() + "], id = " + id);

		Loader<Void> ldr = new DocsAsyncTaskLoader(this);
		Message.show(ldr);

		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void data) {

		Message.show(this);

		setResult(RESULT_OK);
		onCloseDialog(DOCSLIST_LOADER_ID, BUTTON_BACK_ID);

		// ��������� ���� ����������.
		Main.setLastDocsListFetchingTime(new GregorianCalendar());
	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {

		Message.show(this);

		setResult(RESULT_CANCELED);
		onCloseDialog(DOCSLIST_LOADER_ID, BUTTON_BACK_ID);

		// ��������� ���� ����������.
		Main.setLastDocsListFetchingTime(new GregorianCalendar());
	}

	// /////////////////////////////////////////////////////
	// AsyncTaskLoader<Void>
	// /////////////////////////////////////////////////////
	private static class DocsAsyncTaskLoader extends AsyncTaskLoader<Void> {

		private Context context;

		private MediaPlayer mp;

		private int rowsTotal;
		private int rowsCounter;

		private DBase dBase;

		public DocsAsyncTaskLoader(Context context) {

			super(context);

			Message.show(this);

			this.context = context;

			rowsTotal = 0;
			rowsCounter = 0;
		}

		@Override
		public void onStartLoading() {

			Message.show(this);

			dBase = new DBase(context);
			dBase.open();

			mp = MediaPlayer.create(context, R.raw.powerup);
			mp.setLooping(false);

			forceLoad();

			super.onStartLoading();
		}

		@Override
		public Void loadInBackground() {

			Message.show(this);

			if (Main.isDemoMode()) {

				// �������� �� ���������������� ������.

				Cursor cursor = dBase.getRowsAll(DBase.TABLE_DOCS_DEMO_NAME,
						DBase.FIELD_STORE_CODE_NAME);
				rowsTotal = cursor.getCount();
				rowsCounter = 0;
				cursor.moveToFirst();
				if (cursor.isFirst()) {

					if (!isStarted() || isAbandoned() || isReset())
						return null;

					++rowsCounter;
					dBase.copyDocRow(DBase.TABLE_DOCS_NAME, cursor);

					setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(),
							rowsCounter, rowsTotal, 0);

					while (cursor.moveToNext()) {

						try {
							TimeUnit.MILLISECONDS.sleep(DEMO_MODE_SLEEP_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (!isStarted() || isAbandoned() || isReset())
							return null;

						++rowsCounter;
						dBase.copyDocRow(DBase.TABLE_DOCS_NAME, cursor);

						setProgress(WEAK_REF_ACTIVITY.get()
								.getProgressHandler(), rowsCounter, rowsTotal,
								0);
					}
				}

			} else {

				// �������� � �������.

				// ��������� �������������� ����������.
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String deviceId = tm.getDeviceId();
				String uriString = connectionString + "?deviceid=" + deviceId;
				try {

					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

					Document domDoc = dBuilder.parse(uriString);
					domDoc.getDocumentElement().normalize();

					// ��������� ���� ����� ����������.
					NodeList docNodes = domDoc
							.getElementsByTagName(DOC_TAG_NAME);
					rowsTotal = docNodes.getLength();
					rowsCounter = 0;

					SQLiteDatabase sqliteDb = dBase.getSQLiteDatabase();

					// ������� ���� ����������.
					for (int docIdx = 0; docIdx < rowsTotal; docIdx++) {

						if (!isStarted() || isAbandoned() || isReset())
							return null;

						Node docNode = docNodes.item(docIdx);

						// ���������� ������ � ���� ���������.
						NamedNodeMap docAttr = docNode.getAttributes();
						Node docNumNode = docAttr
								.getNamedItem(DOC_NUM_TAG_NAME);
						Node docDateTimeNode = docAttr
								.getNamedItem(DOC_DATE_TAG_NAME);
						Node docRowsNode = docAttr
								.getNamedItem(DOC_ROWS_TAG_NAME);

						String docNumValue = docNumNode.getTextContent();
						String docDateTimeValue = docDateTimeNode
								.getTextContent();
						String docRowsValue = docRowsNode.getTextContent();

						// �������������� ���� ��������� �� ���������� ���� �
						// ������� � ������ �����.
						Date gmt = DocsListLoader.dateFormatter
								.parse(docDateTimeValue);
						long docDateTimeEpochValue = gmt.getTime();

						// ���������� ����������� � ��������� � ������ � ������.
						String docComment = "";
						String storeCode = "";
						String storeDescription = "";

						// ������� ���� ����� ���������.
						NodeList docFields = docNode.getChildNodes();
						int docFieldsTotal = docFields.getLength();
						for (int docFieldIdx = 0; docFieldIdx < docFieldsTotal; docFieldIdx++) {

							Node docFieldNode = docFields.item(docFieldIdx);
							String docFieldName = docFieldNode.getNodeName();

							if (docFieldName.equals(COMMENT_TAG_NAME)) {
								// ���������� ����������� ���������.
								docComment = docFieldNode.getTextContent();
							} else if (docFieldName.equals(STORE_TAG_NAME)) {

								// ���������� �������� � ������ ���������.
								NamedNodeMap storeAttr = docFieldNode
										.getAttributes();

								// ���������� ���� ������.
								Node storeCodeNode = storeAttr
										.getNamedItem(STORE_CODE_TAG_NAME);
								storeCode = storeCodeNode.getTextContent();

								// ���������� ������������� (������������)
								// ������.
								NodeList storeFields = docFieldNode
										.getChildNodes();
								int storeFieldsTotal = storeFields.getLength();
								for (int storeFieldIdx = 0; storeFieldIdx < storeFieldsTotal; storeFieldIdx++) {
									Node storeFieldNode = storeFields
											.item(storeFieldIdx);
									String storeFieldName = storeFieldNode
											.getNodeName();
									if (storeFieldName
											.equals(STORE_DESCR_TAG_NAME)) {
										storeDescription = storeFieldNode
												.getTextContent();
									}
								}
							}
						} // for (int docFieldIdx = 0; docFieldIdx <
							// docFieldsTotal; docFieldIdx++) {

						++rowsCounter;
						ContentValues docsValues = new ContentValues();
						docsValues.put(DBase.FIELD_DOC_NUM_NAME, docNumValue);
						docsValues.put(DBase.FIELD_DOC_DATE_NAME,
								docDateTimeEpochValue);
						docsValues
								.put(DBase.FIELD_DOC_COMMENT_NAME, docComment);
						docsValues.put(DBase.FIELD_DOC_ROWS_NAME, docRowsValue);
						docsValues.put(DBase.FIELD_STORE_CODE_NAME, storeCode);
						docsValues.put(DBase.FIELD_STORE_DESCR_NAME,
								storeDescription);

						dBase.insert(sqliteDb, DBase.TABLE_DOCS_NAME,
								docsValues);

					}
				} catch (Exception e) {
					e.printStackTrace();
					// Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
					// .show();
				}
			} // if (Main.isDemoMode()) {

			mp.start();

			return null;
		}

		@Override
		public void onStopLoading() {

			Message.show(this);

			cancelLoad();

			super.onStopLoading();

			onReleaseResources();
		}

		@Override
		public void onCanceled(Void data) {

			Message.show(this);

			super.onCanceled(data);

			onReleaseResources();
		}

		@Override
		protected void onReset() {

			Message.show(this);

			super.onReset();

			stopLoading();

			onReleaseResources();
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
		private void setProgress(ProgressHandler progressHandler, int what,
				int arg1, int arg2) {

			Message.show("[hashCode = " + this.hashCode()
					+ ", progressHandler = " + progressHandler);

			// ��������� ��������� ����������.
			if (progressHandler != null) {

				android.os.Message msg = new android.os.Message();
				msg.what = what;
				msg.arg1 = arg1;
				msg.arg2 = arg2;

				progressHandler.sendMessage(msg);
			}
		}

		// @Override
		// public void deliverResult(Void data) {
		//
		// Message.show(this);
		//
		// super.deliverResult(data);
		// }

		// @Override
		// public void onAbandon() {
		//
		// Message.show(this);
		//
		// super.onAbandon();
		// }

		// @Override
		// public void onContentChanged() {
		//
		// Message.show(this);
		//
		// super.onContentChanged();
		// }

		// @Override
		// public void onForceLoad() {
		//
		// Message.show(this);
		//
		// super.onForceLoad();
		// }
	}

	@Override
	public void onBackPressed() {

		Message.show(this);

		super.onBackPressed();

		onCloseDialog(DOCSLIST_LOADER_ID, BUTTON_BACK_ID);
	}

	/**
	 * ���������� ������ �� ������� �������.
	 * 
	 * @return
	 */
	public ProgressHandler getProgressHandler() {

		// Message.show(this);

		return progressHandler;
	}

	// ��������� ��������-�������.
	public void onCloseDialog(int dialogId, int buttonId) {

		Message.show("[hashCode = " + this.hashCode() + "dialogId = "
				+ dialogId + ", buttonId = " + buttonId);

		getSupportLoaderManager().getLoader(DOCSLIST_LOADER_ID).abandon();

		finish();
	}
}
