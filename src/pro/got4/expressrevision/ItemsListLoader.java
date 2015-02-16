package pro.got4.expressrevision;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pro.got4.expressrevision.dialogs.ProgressDialogFragment;
import pro.got4.expressrevision.dialogs.ProgressDialogFragment.DialogListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
 * Асинхронный загрузчик, выводящий прогресс-диалог.
 * 
 * @author programmer
 * 
 */
public class ItemsListLoader extends FragmentActivity implements
		LoaderCallbacks<JSONObject>, DialogListener {

	public static final int ID = 500;
	public static final int BUTTON_BACK_ID = 501;

	private static final int DEMO_MODE_SLEEP_TIME = 50;

	public static final String ITEMSLIST_LOADER_TAG = "itemslistloaderfragmentactivity_tag";

	public static final String NUMBER_OF_TREADS_FIELD_NAME = "itemsListLoadedNumberOfUsingThreads";
	public static final String UPLOADING_FLAG_FIELD_NAME = "uploadingFlag";
	public static final String FIELD_STATUS_NAME = "status";
	public static final String FIELD_SERVER_OPERATION_DATE_NAME = "date";
	public static final String FIELD_SERVER_MESSAGE_NAME = "error";
	public static final String FIELD_RESULT_NAME = "result";

	private static final String FIELD_DOC_LOADED_ROWS_TOTAL_NAME = "docRowsTotal";
	private static final String FIELD_DOC_ROWS_TOTAL_NAME = "docLoadedRowsTotal";

	private static final String FIELD_DEVICE_ID_NAME = "deviceId";
	private static final String FIELD_DOC_NUM_NAME = "docNum";
	private static final String FIELD_DOC_DATE_NAME = "docDate";
	private static final String FIELD_CONNECTION_STRING_NAME = "connectionString";

	private static final String FIELD_FIRST_LOADED_ROW_NUM_NAME = "firstRow";
	private static final String FIELD_LAST_LOADED_ROW_NUM_NAME = "lastRow";
	private static final String FIELD_ROWS_IN_EACH_PARCEL_NAME = "rowsInDataParcel";

	// Имя поля, которое в экстрах хранится строка соединения.
	public static final String CONNECTION_STRING_FIELD_NAME = "connectionStringItems";
	public static final String CONNECTION_STRING_UPLOAD_PREFS_FIELD_NAME = "connectionStringItemsPost";

	// Поля XML-парсера.
	private static final String DOC_TAG_NAME = "doc";
	private static final String DOC_NUM_TAG_NAME = "docNum";
	private static final String DOC_DATE_TAG_NAME = "docDate";

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

	// Поле JSON-объекта, хранящее данные о номере строке и количестве
	// номенклатуры для неё.
	private static final String DOC_ROW_QUANT_TAG_NAME = "row_quants";

	// Форматтер даты для преобразования в формат, используемый для формирования
	// идентификатора документа.
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	// Форматтер даты для преобразования к виду, используемому в HTTP-запросах.
	private static final SimpleDateFormat uriDateFormatter = new SimpleDateFormat(
			"yyyyMMddHHmmss", Locale.getDefault());

	// Во избежание утечек (см. подсказку, которая появляется, если класс
	// хэндлера не делать статическим).
	private static WeakReference<ItemsListLoader> WEAK_REF_ACTIVITY;

	// //////////////////////////////////////////////////////////////////////
	// НЕСТАТИЧЕСКИЕ ПОЛЯ, значения которых подлежат восстановлению при
	// реконфигурации активности.
	//
	private ProgressDialogFragment pDialog; // Используется в хэндлере.

	private ProgressHandler progressHandler; // Используется в AsyncTaskLoader.

	private String connectionString;
	private Long docDate = Long.valueOf(0);
	private String docNum = "";
	private int docRowsTotal = 0;

	private int docLoadedRowsTotal = 0; // Количество загруженных строк по всем
										// загрузчикам.

	private int threadsNumber = 0;
	private int maxRowsInParcel = 0;
	private boolean uploading;

	// /////////////////////////////////////////////////////
	// Хэндлер.
	// /////////////////////////////////////////////////////

	/**
	 * Хэндлер, принимающий сообщения о статусе текущей загрузки.
	 */
	private static class ProgressHandler extends Handler {

		private WeakReference<ItemsListLoader> wrActivity;

		public ProgressHandler(WeakReference<ItemsListLoader> wrActivity) {

			this.wrActivity = wrActivity;
		}

		/**
		 * Установка активности, для UI которой хэндлер выполняет действия.
		 * 
		 * @param wrActivity
		 */
		public void setActivity(WeakReference<ItemsListLoader> wrActivity) {

			this.wrActivity = wrActivity;
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			// Установка значения прогресса.
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

		if (!Main.isDemoMode() && !Main.isNetworkAvailable(this, true)) {
			finish();
		}

		uploading = false;
		int status = 0;
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
			docDate = extras.getLong(DBase.FIELD_DOC_DATE_NAME);
			docRowsTotal = (int) extras.getLong(DBase.FIELD_DOC_ROWS_NAME);

			uploading = extras.getBoolean(UPLOADING_FLAG_FIELD_NAME);
			status = extras.getInt(FIELD_STATUS_NAME);
		}

		WEAK_REF_ACTIVITY = new WeakReference<ItemsListLoader>(this);

		// Поиск диалога, ранее созданного для данной активности.
		pDialog = (ProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(ITEMSLIST_LOADER_TAG);

		// Если диалог еще не создавался, то его нужно создать.
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

			// В демо-режиме достаточно одного потока.
			threadsNumber = 1;

		} else {

			threadsNumber = Integer.valueOf(PreferenceManager
					.getDefaultSharedPreferences(this).getString(
							NUMBER_OF_TREADS_FIELD_NAME, "1"));
		}

		// Максимальное количество строк в пакете.
		maxRowsInParcel = Integer.valueOf(PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						FIELD_ROWS_IN_EACH_PARCEL_NAME, "100"));

		// Каждый загрузчик/выгрузчик получает набор параметров:
		// - номер, дата документа;
		// - строка соединения.
		Bundle exchangeArgs = new Bundle();
		exchangeArgs.putString(FIELD_DOC_NUM_NAME, docNum);
		exchangeArgs.putLong(FIELD_DOC_DATE_NAME, docDate);
		exchangeArgs.putString(FIELD_CONNECTION_STRING_NAME, connectionString);
		exchangeArgs.putBoolean(UPLOADING_FLAG_FIELD_NAME, uploading);
		exchangeArgs.putInt(FIELD_STATUS_NAME, status);

		if (uploading) {

			// Выгрузка данных на сервер производится единственным загрузчиком.
			getSupportLoaderManager().initLoader(0, exchangeArgs, this);

		} else {

			// Загрузка данных с сервера производится набором загрузчиков.
			// Разбивка загружаемых строк на интервалы, каждый из которых будет
			// загружаться отдельным запросом.
			SparseArray<int[]> spArr = sliceInterval(docRowsTotal,
					threadsNumber, maxRowsInParcel);

			// На основании разбивки по интервалам каждый загрузчик
			// дополнительно получает:
			// - номер первой строки документа, которую он должен загрузить.
			// - номер последней строки документа, которую он должен
			// загрузить.
			// - желательное количество строк в одном загружаемом пакете.
			for (int i = 0; i < spArr.size(); i++) {

				exchangeArgs.putInt(FIELD_FIRST_LOADED_ROW_NUM_NAME,
						spArr.get(i)[0] + 1);
				exchangeArgs.putInt(FIELD_LAST_LOADED_ROW_NUM_NAME,
						spArr.get(i)[1] + 1);
				exchangeArgs.putInt(FIELD_ROWS_IN_EACH_PARCEL_NAME,
						maxRowsInParcel);

				getSupportLoaderManager().initLoader(i, exchangeArgs, this);
			}
		}
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

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		outState.putInt(FIELD_DOC_LOADED_ROWS_TOTAL_NAME, docLoadedRowsTotal);
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<JSONObject> onCreateLoader(int id, Bundle args) {

		boolean uploading = args.getBoolean(UPLOADING_FLAG_FIELD_NAME);

		Loader<JSONObject> ldr;
		if (uploading) {
			ldr = new ItemsAsyncTaskUploader(this, args);
		} else {
			ldr = new ItemsAsyncTaskLoader(this, args);
		}
		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {

		if (uploading) {

			// При выгрузке здесь проверки не производится, а в результат
			// устанавливается строка ответа сервера.
			// Объект data может уже содержать в себе результат работы
			// активности.
			// Но если его там нет, то считается, что всё нормально.
			int resultCode = RESULT_OK;
			try {
				resultCode = (Integer) data.get(FIELD_RESULT_NAME);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			// Объект data нужно передать вызывающей активности для дальнейшего
			// анализа.
			Intent intent = new Intent();
			intent.putExtra(FIELD_RESULT_NAME, data.toString());

			setResult(resultCode, intent);

		} else {

			// При загрузке сервера проверка состоит в том, что сверяется
			// количество
			// строк документа с фактически загруженным количеством строк.
			if (docRowsTotal == docLoadedRowsTotal) {
				setResult(RESULT_OK);
			} else {
				setResult(RESULT_CANCELED);
			}
		}

		onCloseDialog(ID, BUTTON_BACK_ID);
	}

	@Override
	public void onLoaderReset(Loader<JSONObject> loader) {

		setResult(RESULT_CANCELED);
		onCloseDialog(ID, BUTTON_BACK_ID);

	}

	// /////////////////////////////////////////////////////
	// AsyncTaskLoader<Void>
	// /////////////////////////////////////////////////////
	/**
	 * Класс, реализующий загрузку содержимого документов.
	 * 
	 * @author programmer
	 * 
	 */
	private static class ItemsAsyncTaskLoader extends
			AsyncTaskLoader<JSONObject> {

		private Context context;

		private MediaPlayer mp;

		private DBase dBase;

		private String docNum;
		private long docDate;
		private String connectionString;

		// Интервалы строк к получению.
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

				// Количество строк к получению в данном загрузчике.
				int rowsTotal = lastLoadersRowNumber - firstLoadersRowNumber
						+ 1;

				// Количество запросов, необходимых для получения требуемого
				// количества строк (с округлением вверх).
				int queries = (int) Math.ceil((double) rowsTotal
						/ maxRowsInParcel);

				// Исходя из полученного интервала строк и количества строк в
				// пакете производится разбивка строк по пакетам.
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
		public JSONObject loadInBackground() {

			JSONObject jsonResponse = new JSONObject();

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

							// В случае ошибки возвращаем её описание.
							try {

								jsonResponse.put(FIELD_RESULT_NAME,
										RESULT_CANCELED);
								jsonResponse.put(FIELD_SERVER_MESSAGE_NAME,
										e.getMessage());

								return jsonResponse;

							} catch (JSONException e1) {
								e1.printStackTrace();
							}
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
					} // while (cursor.moveToNext()) {
				} // if (cursor.isFirst()) {

				try {
					jsonResponse.put(FIELD_RESULT_NAME, RESULT_OK);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				mp.start();

				return jsonResponse;

			} else {

				// Загрузка с сервера.
				// Итоговая строка должна иметь вид:
				// Если задано ограничение строк:
				// http://express.nsk.ru:9999/eritems.php?deviceid=12345
				// &docdate=20150105162307&docnum=%27%D0%AD%D0%9A%D0%A100000008%27
				// &firstrow=1&lastrow=10
				// Если не задано ограничение строк:
				// http://express.nsk.ru:9999/eritems.php?deviceid=12345&docdate=20141223120310&docnum="ЭКС00000001"

				// Получение идентификатора устройства.
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String deviceId = tm.getDeviceId();

				// Дата в БД имеет вид миллисекунд с начала Юникс-эпохи,
				// и её следует преобразовать к виду "20140101000000".
				String dateURIFormattedString = uriDateFormatter
						.format(docDate);
				String query = "?" + FIELD_DEVICE_ID_NAME + "=" + deviceId
						+ "&" + FIELD_DOC_DATE_NAME + "="
						+ dateURIFormattedString + "&" + FIELD_DOC_NUM_NAME
						+ "='";

				// В номере документа может содержаться кириллица, поэтому
				// необходимо преобразование.
				try {
					query = query + URLEncoder.encode(docNum, HTTP.UTF_8) + "'";
				} catch (UnsupportedEncodingException e) {

					e.printStackTrace();

					// В случае ошибки возвращаем её описание.
					try {

						jsonResponse.put(FIELD_RESULT_NAME, RESULT_CANCELED);
						jsonResponse.put(FIELD_SERVER_MESSAGE_NAME,
								e.getMessage());

						return jsonResponse;

					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();

				try {

					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

					// Перебор интервалов и исполнение запроса по каждому из
					// них.
					for (int i = 0; i < spArr.size(); i++) {

						String queryRowsInterval = "&firstrow="
								+ (spArr.get(i)[0] + 1) + "&lastrow="
								+ (spArr.get(i)[1] + 1);

						String uriString = connectionString + query
								+ queryRowsInterval;

						// Непосредственно момент загрузки через сеть.
						Document domDoc = dBuilder.parse(uriString);
						domDoc.getDocumentElement().normalize();

						// Парсинг.
						// Получение всех узлов документов. Т.к. за раз всегда
						// извлекается содержимое только одного документа, то и
						// узел всегда один.
						parseDocument(domDoc, dBase);

						try {
							jsonResponse.put(FIELD_RESULT_NAME, RESULT_OK);
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				} catch (Exception e) {

					e.printStackTrace();

					// В случае ошибки возвращаем её описание.
					try {

						jsonResponse.put(FIELD_RESULT_NAME, RESULT_CANCELED);
						jsonResponse.put(FIELD_SERVER_MESSAGE_NAME,
								e.getMessage());

						return jsonResponse;

					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}

			} // if (Main.isDemoMode()) {

			mp.start();

			return jsonResponse;
		}

		@Override
		public void deliverResult(JSONObject data) {
			super.deliverResult(data);
		}

		@Override
		public void onStopLoading() {

			cancelLoad();

			super.onStopLoading();

			onReleaseResources();
		}

		@Override
		public void onCanceled(JSONObject data) {

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
		 * Освобождение ресурсов.
		 */
		private void onReleaseResources() {
			if (mp != null)
				mp.release();
		}

		/**
		 * Установка состояния индикатора.
		 */
		private void setProgress(ProgressHandler progressHandler,
				int progressIncrement, int max, int indeterminate) {

			// Установка состояния индикатора.
			if (progressHandler != null) {

				android.os.Message msg = new android.os.Message();
				msg.what = progressIncrement;
				msg.arg1 = max;
				msg.arg2 = indeterminate;

				progressHandler.sendMessage(msg);
			}
		}

		/**
		 * Парсинг DOM-документа, содержащего XML с построчной его записью в БД.
		 * 
		 * @return количество записанных строк.
		 */
		private int parseDocument(Document domDoc, DBase dBase) {

			// Количество строк, загруженных в данном вызове.
			int rowsCounter = 0;

			NodeList docNodes = domDoc.getElementsByTagName(DOC_TAG_NAME);

			// Извлечение номера и даты документа.
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

			// Получение всех узлов строк.
			NodeList rowNodes = domDoc.getElementsByTagName(ROW_TAG_NAME);
			int rows = rowNodes.getLength();

			// Перебор всех строк.
			for (int rowIdx = 0; rowIdx < rows; rowIdx++) {

				if (!isStarted() || isAbandoned() || isReset())
					return rowsCounter;

				Node rowNode = rowNodes.item(rowIdx);

				// Извлечение номера строки.
				NamedNodeMap rowAttr = rowNode.getAttributes();
				Node rowNumNode = rowAttr.getNamedItem(ROW_NUM_TAG_NAME);

				String rowNumValue = rowNumNode.getTextContent();

				// Извлечение содержимого строки.
				String itemCode = ""; // Код номенклатуры.
				String itemDescr = ""; // Наименование номенклатуры.
				String itemDescrFull = ""; // Полное наименование
											// номенклатуры.
				int itemUseSpecif = 0; // Вести учет по
										// характеристикам.
				String specifCode = ""; // Код характеристики
										// номенклатуры.
				String specifDescr = ""; // Наименование
											// характеристики
											// номенклатуры.
				String measurDescr = ""; // Наименование единицы
											// измерения.
				float price = 0; // Цена.
				float quantAcc = 0; // Количество в учете.
				float quant = 0; // Количество по инвентаризации.

				// Перебор всех полей строки.
				NodeList rowFields = rowNode.getChildNodes();
				int rowFieldsTotal = rowFields.getLength();
				for (int rowFieldIdx = 0; rowFieldIdx < rowFieldsTotal; rowFieldIdx++) {

					Node rowFieldNode = rowFields.item(rowFieldIdx);
					String rowFieldName = rowFieldNode.getNodeName();

					if (rowFieldName.equals(ITEM_TAG_NAME)) {

						// Получение кода номенклатуры.
						NamedNodeMap itemAttr = rowFieldNode.getAttributes();

						Node itemCodeNode = itemAttr
								.getNamedItem(ITEM_CODE_TAG_NAME);
						itemCode = itemCodeNode.getTextContent();

						// Перебор всех полей номенклатуры.
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

						// Получение кода характеристики
						// номенклатуры.
						NamedNodeMap specifAttr = rowFieldNode.getAttributes();

						Node specifCodeNode = specifAttr
								.getNamedItem(SPECIF_CODE_TAG_NAME);
						specifCode = specifCodeNode.getTextContent();

						// Перебор всех полей характеристики
						// номенклатуры.
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

						// Перебор всех полей единицы измерения.
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

				// Идентификатор документа состоит из даты документа
				// и его номера.
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

				long lastId = dBase.insert(dBase.getSQLiteDatabase(),
						DBase.TABLE_ITEMS_NAME, itemValues);

				++rowsCounter;

				WEAK_REF_ACTIVITY.get().incrementDocLoadedRowsTotal(1);

				setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(), 1,
						WEAK_REF_ACTIVITY.get().getDocRowsTotal(), 0);
			}

			return rowsCounter;
		}
	}

	/**
	 * Класс, реализующий выгрузку содержимого документов.
	 * 
	 * @author programmer
	 * 
	 */
	private static class ItemsAsyncTaskUploader extends
			AsyncTaskLoader<JSONObject> {

		private Context context;

		private MediaPlayer mp;

		private DBase dBase;

		private String docNum;
		private long docDate;
		private int status;
		private String connectionString;
		String deviceId;

		// Интервалы строк к выгрузке.
		SparseArray<int[]> spArr;

		public ItemsAsyncTaskUploader(Context context, Bundle args) {

			super(context);

			if (args != null) {

				docNum = args.getString(FIELD_DOC_NUM_NAME);
				docDate = args.getLong(FIELD_DOC_DATE_NAME);
				status = args.getInt(FIELD_STATUS_NAME);
				connectionString = args.getString(FIELD_CONNECTION_STRING_NAME);

				int firstLoadersRowNumber = args
						.getInt(FIELD_FIRST_LOADED_ROW_NUM_NAME);
				int lastLoadersRowNumber = args
						.getInt(FIELD_LAST_LOADED_ROW_NUM_NAME);
				int maxRowsInParcel = args
						.getInt(FIELD_ROWS_IN_EACH_PARCEL_NAME);

				// Количество строк к отправке в данном экземлпяре задачи.
				int rowsTotal = lastLoadersRowNumber - firstLoadersRowNumber
						+ 1;

				// Количество запросов, необходимых для отправки требуемого
				// количества строк (с округлением вверх).
				int queries = (int) Math.ceil((double) rowsTotal
						/ maxRowsInParcel);

				// Исходя из полученного интервала строк и количества строк в
				// пакете производится разбивка строк по пакетам.
				spArr = sliceInterval(rowsTotal, queries, maxRowsInParcel);

				// Получение идентификатора устройства.
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				deviceId = tm.getDeviceId();
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
		public JSONObject loadInBackground() {

			JSONObject jsonResponse = new JSONObject();

			if (Main.isDemoMode()) {

				// Имитация ответа, возвращаемого сервером.
				// {"date":"2015-02-13 21:53:40",
				// "deviceId":"356633059239416",
				// "docNum":"ЭКС00000004",
				// "docDate":"2015-02-04 18:02:19",
				// "status":"3"}
				try {

					Thread.sleep(2000);

					String demoServerDate = dateFormatter.format(new Date());

					jsonResponse.put(FIELD_SERVER_OPERATION_DATE_NAME,
							demoServerDate);
					jsonResponse.put(FIELD_DEVICE_ID_NAME, deviceId);
					jsonResponse.put(FIELD_DOC_NUM_NAME, docNum);
					jsonResponse.put(FIELD_DOC_DATE_NAME, docDate);
					jsonResponse.put(FIELD_STATUS_NAME, status);
					jsonResponse.put(FIELD_RESULT_NAME, RESULT_OK);

					mp.start();

				} catch (Exception e) {

					e.printStackTrace();

					// В случае ошибки возвращаем её описание.
					try {

						jsonResponse.put(FIELD_RESULT_NAME, RESULT_CANCELED);
						jsonResponse.put(FIELD_SERVER_MESSAGE_NAME,
								e.getMessage());

						return jsonResponse;

					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}

			} else {

				// Получение записей документа и заполнение ими JSON-массива.
				Cursor cursor = dBase.getRowsAll(DBase.TABLE_ITEMS_NAME,
						DBase.FIELD_ROW_NUM_NAME);

				int rowNum_Idx = cursor
						.getColumnIndex(DBase.FIELD_ROW_NUM_NAME);
				int quant_Idx = cursor.getColumnIndex(DBase.FIELD_QUANT_NAME);

				cursor.moveToFirst();
				if (!cursor.isFirst())
					return null;

				int rowNum;
				float quant;
				rowNum = cursor.getInt(rowNum_Idx);
				quant = cursor.getFloat(quant_Idx);

				JSONArray arr = new JSONArray();
				try {
					arr.put(rowNum);
					arr.put(quant);
				} catch (JSONException e) {

					e.printStackTrace();

					// В случае ошибки возвращаем её описание.
					try {

						jsonResponse.put(FIELD_RESULT_NAME, RESULT_CANCELED);
						jsonResponse.put(FIELD_SERVER_MESSAGE_NAME,
								e.getMessage());

						return jsonResponse;

					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}

				boolean error = false;
				while (cursor.moveToNext()) {
					rowNum = cursor.getInt(rowNum_Idx);
					quant = cursor.getFloat(quant_Idx);
					try {
						arr.put(rowNum);
						arr.put(quant);
					} catch (JSONException e) {
						e.printStackTrace();

						// В случае ошибки возвращаем её описание.
						try {

							jsonResponse
									.put(FIELD_RESULT_NAME, RESULT_CANCELED);
							jsonResponse.put(FIELD_SERVER_MESSAGE_NAME,
									e.getMessage());

							return jsonResponse;

						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}
				}

				try {

					JSONObject jsonObj = new JSONObject();
					jsonObj.put(FIELD_DEVICE_ID_NAME, deviceId);

					String dateURIFormattedString = uriDateFormatter
							.format(docDate);
					jsonObj.put(FIELD_DOC_DATE_NAME, dateURIFormattedString);

					// В номере документа может содержаться кириллица, поэтому
					// необходимо бы преобразование.
					// Но в случае с JSON используется UTF-8 и преобразовывать
					// его не нужно.
					jsonObj.put(FIELD_DOC_NUM_NAME, docNum);

					// Статус, который должен получить документ после
					// заполнения.
					jsonObj.put(FIELD_STATUS_NAME, status);

					// Массив, содержащий данные о номере строки и количестве
					// в ней.
					jsonObj.put(DOC_ROW_QUANT_TAG_NAME, arr);

					jsonResponse = sendHttpJsonObject(connectionString, jsonObj);
					jsonResponse.put(FIELD_RESULT_NAME, RESULT_OK);

					mp.start();

				} catch (Exception e) {

					e.printStackTrace();

					// В случае ошибки возвращаем её описание.
					try {

						jsonResponse.put(FIELD_RESULT_NAME, RESULT_CANCELED);
						jsonResponse.put(FIELD_SERVER_MESSAGE_NAME,
								e.getMessage());

						return jsonResponse;

					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
			return jsonResponse;
		}

		@Override
		public void onCanceled(JSONObject data) {

			super.onCanceled(data);

			onReleaseResources();
		}

		@Override
		public void deliverResult(JSONObject data) {
			super.deliverResult(data);
		}

		@Override
		public void onStopLoading() {

			cancelLoad();

			super.onStopLoading();

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
		 * Освобождение ресурсов.
		 */
		private void onReleaseResources() {
			if (mp != null)
				mp.release();
		}

		/**
		 * Установка состояния индикатора.
		 */
		private void setProgress(ProgressHandler progressHandler,
				int progressIncrement, int max, int indeterminate) {

			// Установка состояния индикатора.
			if (progressHandler != null) {

				android.os.Message msg = new android.os.Message();
				msg.what = progressIncrement;
				msg.arg1 = max;
				msg.arg2 = indeterminate;

				progressHandler.sendMessage(msg);
			}
		}

		/**
		 * Загрузка JSONObject на сервер.
		 * 
		 * @param
		 */
		public JSONObject sendHttpJsonObject(String url, JSONObject jsonObj)
				throws Exception {

			HttpURLConnection connection = null;

			try {

				URL object = new URL(url);
				connection = (HttpURLConnection) object.openConnection();

				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setUseCaches(false);
				connection.setRequestMethod("POST");

				// Формат и способ представления сущности.
				connection.setRequestProperty("Content-Type",
						"application/json");
				// Список допустимых форматов ресурса.
				connection.setRequestProperty("Accept", "application/json");

				OutputStreamWriter streamWriter = new OutputStreamWriter(
						connection.getOutputStream());

				streamWriter.write(jsonObj.toString());
				streamWriter.flush();

				StringBuilder stringBuilder = new StringBuilder();

				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					InputStreamReader streamReader = new InputStreamReader(
							connection.getInputStream());
					BufferedReader bufferedReader = new BufferedReader(
							streamReader);

					String response = null;
					while ((response = bufferedReader.readLine()) != null) {
						stringBuilder.append(response + "\n");
					}
					bufferedReader.close();

					String jsonString = stringBuilder.toString();
					// System.out.println(stringBuilder.toString());

					return new JSONObject(jsonString);

				} else {

					// System.out.println(connection.getResponseMessage());
					throw new Exception(connection.getResponseMessage());
					// return connection.getResponseMessage();
				}
			} catch (Exception e) {
				// System.out.println(exception.toString());
				throw e;
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		/**
		 * Загрузка файла на сервер (ОБРАЗЕЦ).
		 * 
		 * @param
		 */
		private void sendHttpFile(String fileName) {

			String urlString = "http://express.nsk.ru:9999/_up_au.php";

			HttpURLConnection httpUrlConnection = null;
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			String twoHyphens = "--";
			String boundary = "*****";
			String lineEnd = "\r\n"; // !!! Важно!

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1 * 1024 * 1024;

			String responseFromServer = "";
			try {
				// Отправка данных на сервер POST-запросом.
				FileInputStream fileInputStream = new FileInputStream(new File(
						fileName));

				URL url = new URL(urlString);

				httpUrlConnection = (HttpURLConnection) url.openConnection();

				// Для соединения разрешен ввод.
				httpUrlConnection.setDoInput(true);

				// Для соединения разрешен вывод.
				httpUrlConnection.setDoOutput(true);

				// Кэш не использовать.
				httpUrlConnection.setUseCaches(false);

				// Команда, которая будет послана на HTTP-сервер.
				httpUrlConnection.setRequestMethod("POST");

				// Установка свойств полей заголовка.
				// Постоянное соединение
				// (использование одного TCP-соединения для отправки и получения
				// множественных HTTP-запросов и ответов вместо открытия нового
				// соединения для каждой пары запрос-ответ).
				httpUrlConnection
						.setRequestProperty("Connection", "Keep-Alive");

				// Единственное значение, позволяющее передачу файла в
				// POST-запросе.
				// Тип содержимого multipart/form-data — это составной тип
				// содержимого, чаще всего использующийся для отправки HTML-форм
				// с бинарными (не-ASCII) данными методом POST протокола HTTP.
				httpUrlConnection.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);

				// Поток для записи данных в URL-соединение.
				OutputStream oStream = httpUrlConnection.getOutputStream();

				// Оболочка существующего потока, записывающая в него
				// типизированные данные.
				dataOutputStream = new DataOutputStream(oStream);
				dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);

				// Способ распределения сущностей в сообщении при передаче
				// нескольких фрагментов.
				dataOutputStream
						.writeBytes("Content-Disposition:form-data; name=\"ufile\";filename=\""
								+ fileName + "\"" + lineEnd);
				dataOutputStream.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {
					dataOutputStream.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				// send multipart form data necesssary after file data...
				dataOutputStream.writeBytes(lineEnd);
				dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens
						+ lineEnd);
				// close streams
				System.out.println("File is written");
				fileInputStream.close();
				dataOutputStream.flush();
				dataOutputStream.close();
			} catch (Exception e) {
				System.out.println("error: " + e);
			}

			// ------------------ read the SERVER RESPONSE
			try {
				InputStream is = httpUrlConnection.getInputStream();
				dataInputStream = new DataInputStream(is);
				String str;

				while ((str = dataInputStream.readLine()) != null) {
					System.out.println("Server Response " + str);
				}
				dataInputStream.close();
			} catch (Exception e) {
				System.out.println("error: " + e);
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

		// Останавливаются все загрузчики.
		for (int i = 0; i < threadsNumber; i++) {

			Loader<Void> loader = getSupportLoaderManager().getLoader(i);

			if (loader != null)
				loader.abandon();
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
	 * Распределеяет интервал на поддиапазоны, принимая в расчет максимально
	 * допустимое количество элементов в каждом диапазоне.
	 * 
	 * @param itemsTotal
	 *            количество элементов
	 * @param numberOfIntervals
	 *            количество подинтервалов, в которые д.б. распределены элементы
	 * @param itemsPerInterval
	 *            желаемое количество элементов в интервале. Если ==0, то
	 *            элементы будут равномерно распределены по поддиапазонам.
	 * @return SparseArray, каждый элемент которого содержит массив из двух
	 *         элементов.<br>
	 *         Первый - это индекс первого элемента поддиапазона;<br>
	 *         Второй - это индекс последнего элемента поддиапазона.
	 */
	private static SparseArray<int[]> sliceInterval(int itemsTotal,
			int numberOfIntervals, int itemsPerInterval) {

		SparseArray<int[]> spArray = new SparseArray<int[]>();

		int lastIntervalItemIdx = -1;

		for (int i = 0; i < numberOfIntervals; i++) {

			// Интервалов осталось:
			int intervalsLeft = numberOfIntervals - i;

			// Нераспределенных элементов осталось:
			int itemsLeft = itemsTotal - (lastIntervalItemIdx + 1);

			// Если нераспределенных строк больше нет, то дальше распределять
			// создавать не нужно.
			if (itemsLeft <= 0)
				break;

			// Индекс первого элемента интервала очередного поддиапазона.
			int firstIntervalItemIdx = lastIntervalItemIdx + 1;

			// Элементов, приходящихся на каждый из оставшихся интервалов
			// (с округлением до большего):
			int itemsLeftPerInterval = (int) Math.ceil((double) itemsLeft
					/ intervalsLeft);

			// Если количество элементов, приходящихся на интервал оказалось
			// меньше допустимого количества элементов в пакете, то на интервал
			// должно приходиться именно допустимое количество.
			// Это нужно для того, чтобы интервалы содержали максимально
			// допустимое количество элементов, а не просто усредненное.
			// В противном случае, если интервалов много, то они будут мелкими,
			// что м.б. неэффективно.
			if (itemsPerInterval != 0
					&& itemsLeftPerInterval < itemsPerInterval)
				itemsLeftPerInterval = itemsPerInterval;

			// Индекс последнего элемента интервала очередного поддиапазона.
			lastIntervalItemIdx = firstIntervalItemIdx + itemsLeftPerInterval
					- 1;

			// Последний элемент не может выходить за пределы всего количества
			// элементов.
			if (lastIntervalItemIdx > itemsTotal - 1)
				lastIntervalItemIdx = itemsTotal - 1;

			int[] arr = { firstIntervalItemIdx, lastIntervalItemIdx };

			spArray.put(i, arr);
		}

		return spArray;
	}
}
