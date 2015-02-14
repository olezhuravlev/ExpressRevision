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
 * Асинхронный загрузчик, выводящий прогресс-диалог.
 * 
 * @author programmer
 * 
 */
public class DocsListLoader extends FragmentActivity implements
		LoaderCallbacks<Void>, DialogListener {

	public static final int ID = 200;
	public static final int BUTTON_BACK_ID = 201;

	private static final int DEMO_MODE_SLEEP_TIME = 500;

	public static final String DOCSLIST_LOADER_TAG = "docslistloaderfragmentactivity_tag";

	private ProgressDialogFragment pDialog; // Используется в хэндлере.

	private ProgressHandler progressHandler; // Используется в AsyncTaskLoader.

	// Имя поля, которое в экстрах хранит строку соединения.
	public static final String CONNECTION_STRING_FIELD_NAME = "connectionStringDocs";

	// Поля XML-парсера.
	private static final String FIELD_DEVICE_ID_NAME = "deviceId";
	private static final String DOC_TAG_NAME = "doc";
	private static final String DOC_NUM_TAG_NAME = "docNum";
	private static final String DOC_DATE_TAG_NAME = "docDate";
	private static final String DOC_ROWS_TAG_NAME = "rows";

	private static final String STORE_TAG_NAME = "store";
	private static final String STORE_CODE_TAG_NAME = "code";
	private static final String STORE_DESCR_TAG_NAME = "descr";

	private static final String COMMENT_TAG_NAME = "comm";

	private static String connectionString;

	// Форматтер даты для преобразования из входного формата.
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	// Во избежание утечек (см. подсказку, которая появляется, если класс
	// хэндлера не делать статическим).
	private static WeakReference<DocsListLoader> WEAK_REF_ACTIVITY;

	// /////////////////////////////////////////////////////
	// Хэндлер.
	// /////////////////////////////////////////////////////

	/**
	 * Хэндлер, принимающий сообщения о статусе текущей загрузки.
	 */
	private static class ProgressHandler extends Handler {

		private WeakReference<DocsListLoader> wrActivity;

		public ProgressHandler(WeakReference<DocsListLoader> wrActivity) {

			this.wrActivity = wrActivity;
		}

		/**
		 * Установка активности, для UI которой хэндлер выполняет действия.
		 * 
		 * @param wrActivity
		 */
		public void setActivity(WeakReference<DocsListLoader> wrActivity) {

			this.wrActivity = wrActivity;
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			// Установка значения прогресса.
			wrActivity.get().pDialog.setIndeterminate(msg.arg2 == 0 ? false
					: true);
			wrActivity.get().pDialog.setIncrementMode(false);
			wrActivity.get().pDialog.setMax(msg.arg1);
			wrActivity.get().pDialog.setProgress(msg.what);

			return;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (!Main.isDemoMode() && !Main.isNetworkAvailable(this, true)) {
			finish();
		}

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this,
					getString(R.string.docsConnectionParametersNotSet),
					Toast.LENGTH_LONG).show();
			finish();
		} else {

			connectionString = extras.getString(CONNECTION_STRING_FIELD_NAME);
			if (!Main.isDemoMode()
					&& (connectionString == null || connectionString.isEmpty())) {
				Toast.makeText(this,
						getString(R.string.docsConnectionStringNotSet),
						Toast.LENGTH_LONG).show();
				finish();
			}
		}

		WEAK_REF_ACTIVITY = new WeakReference<DocsListLoader>(this);

		// Поиск диалога, ранее созданного для данной активности.
		pDialog = (ProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(DOCSLIST_LOADER_TAG);

		// Если диалог еще не создавался, то его нужно создать.
		if (pDialog == null) {

			pDialog = new ProgressDialogFragment();
			pDialog.setTitle(getString(R.string.loadingDocuments));
			pDialog.setMessage(getString(R.string.pleaseWait));

			pDialog.show(getSupportFragmentManager(), DOCSLIST_LOADER_TAG);
		}

		getSupportLoaderManager().initLoader(ID, null, this);
	}

	@Override
	public void onResume() {

		super.onResume();

		// Инициалиация хэндлера прогресс-диалога.
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
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle bndl) {

		Loader<Void> ldr = new DocsAsyncTaskLoader(this);

		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void data) {

		setResult(RESULT_OK);
		onCloseDialog(ID, BUTTON_BACK_ID);

		// Установка даты обновления.
		Main.setLastDocsListFetchingTime(new GregorianCalendar());
	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {

		setResult(RESULT_CANCELED);
		onCloseDialog(ID, BUTTON_BACK_ID);

		// Установка даты обновления.
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

			this.context = context;

			rowsTotal = 0;
			rowsCounter = 0;
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

			// Неопределенный режим индикатора.
			// setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(), 0, 0,
			// 1);

			if (Main.isDemoMode()) {

				// Загрузка из демонстрационных таблиц.

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

				// Загрузка с сервера.

				// Получение идентификатора устройства.
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String deviceId = tm.getDeviceId();
				String uriString = connectionString + "?"
						+ FIELD_DEVICE_ID_NAME + "=" + deviceId;
				try {

					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

					Document domDoc = dBuilder.parse(uriString);
					domDoc.getDocumentElement().normalize();

					// Получение всех узлов документов.
					NodeList docNodes = domDoc
							.getElementsByTagName(DOC_TAG_NAME);
					rowsTotal = docNodes.getLength();
					rowsCounter = 0;

					SQLiteDatabase sqliteDb = dBase.getSQLiteDatabase();

					// Перебор всех документов.
					for (int docIdx = 0; docIdx < rowsTotal; docIdx++) {

						if (!isStarted() || isAbandoned() || isReset())
							return null;

						Node docNode = docNodes.item(docIdx);

						// Извлечение номера и даты документа.
						NamedNodeMap docAttr = docNode.getAttributes();
						Node docNumNode = docAttr
								.getNamedItem(DOC_NUM_TAG_NAME);
						Node docDateTimeNode = docAttr
								.getNamedItem(DOC_DATE_TAG_NAME);
						Node docRowsNode = docAttr
								.getNamedItem(DOC_ROWS_TAG_NAME);

						// Номер и дата документа.
						String docNumValue = docNumNode.getTextContent();
						String docDateTimeValue = docDateTimeNode
								.getTextContent();

						// Преобразование даты документа из строкового вида
						// "2015-01-19 14:50:04" к времени с начала эпохи.
						Date gmt = DocsListLoader.dateFormatter
								.parse(docDateTimeValue);
						long docDateTimeEpochValue = gmt.getTime();

						// Количество строк.
						String docRowsValue = docRowsNode.getTextContent();

						// Извлечение комментария к документу и данных о складе.
						String docComment = "";
						String storeCode = "";
						String storeDescription = "";

						// Перебор всех полей документа.
						NodeList docFields = docNode.getChildNodes();
						int docFieldsTotal = docFields.getLength();
						for (int docFieldIdx = 0; docFieldIdx < docFieldsTotal; docFieldIdx++) {

							Node docFieldNode = docFields.item(docFieldIdx);
							String docFieldName = docFieldNode.getNodeName();

							if (docFieldName.equals(COMMENT_TAG_NAME)) {
								// Извлечение комментария документа.
								docComment = docFieldNode.getTextContent();
							} else if (docFieldName.equals(STORE_TAG_NAME)) {

								// Извлечение сведений о складе документа.
								NamedNodeMap storeAttr = docFieldNode
										.getAttributes();

								// Извлечение кода склада.
								Node storeCodeNode = storeAttr
										.getNamedItem(STORE_CODE_TAG_NAME);
								storeCode = storeCodeNode.getTextContent();

								// Извлечение представления (наименования)
								// склада.
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
						docsValues.put(DBase.FIELD_DOC_ID_NAME, DBase.getDocId(
								docNumValue, docDateTimeEpochValue));

						dBase.insert(sqliteDb, DBase.TABLE_DOCS_NAME,
								docsValues);

					}
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

		/**
		 * Освобождение ресурсов.
		 */
		private void onReleaseResources() {
			if (mp != null)
				mp.release();
		}

		/**
		 * Установка состояния индикатора.
		 */
		private void setProgress(ProgressHandler progressHandler, int progress,
				int max, int indeterminate) {

			// Установка состояния индикатора.
			if (progressHandler != null) {

				android.os.Message msg = new android.os.Message();
				msg.what = progress;
				msg.arg1 = max;
				msg.arg2 = indeterminate;

				progressHandler.sendMessage(msg);
			}
		}
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();

		onCloseDialog(ID, BUTTON_BACK_ID);
	}

	/**
	 * Возвращает ссылку на текущий хендлер.
	 * 
	 * @return
	 */
	public ProgressHandler getProgressHandler() {

		return progressHandler;
	}

	// Слушатель прогресс-диалога.
	public void onCloseDialog(int dialogId, int buttonId) {

		getSupportLoaderManager().getLoader(ID).abandon();

		finish();
	}
}
