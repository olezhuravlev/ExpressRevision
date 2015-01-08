package pro.got4.expressrevision;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
public class ItemsListLoader extends FragmentActivity implements
		LoaderCallbacks<Void>, DialogListener {

	public static final int ITEMSLIST_LOADER_ID = 1;
	public static final String ITEMSLIST_LOADER_TAG = "itemslistloaderfragmentactivity_tag";

	private static final String FIELD_ROWSCOUNTER_NAME = "rowsCounter";
	private static final String FIELD_ROWSTOTAL_NAME = "rowsTotal";

	public static final int BUTTON_BACK_ID = 1;

	private ProgressDialogFragment pDialog; // Используется в хэндлере.

	private ProgressHandler progressHandler; // Используется в AsyncTaskLoader.

	private static final int DEMO_MODE_SLEEP_TIME = 20;

	// Имя поля, которое в экстрах хранится строка соединения.
	public static final String CONNECTION_STRING_FIELD_NAME = "connection_string";

	private static String connectionString;
	private static Long docDate = Long.valueOf(0);
	private static String docNum = "";

	private static int rowsCounter;
	private static int rowsTotal;

	// Поля XML-парсера.
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

	// Форматтер даты для преобразования в формат, используемый для формирования
	// идентификатора документа.
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	// Во избежание утечек (см. подсказку, которая появляется, если класс
	// хэндлера не делать статическим).
	private static WeakReference<ItemsListLoader> WEAK_REF_ACTIVITY;

	// /////////////////////////////////////////////////////
	// Хэндлер.
	// /////////////////////////////////////////////////////

	/**
	 * Хэндлер, принимающий сообщения о статусе текущей загрузки.
	 */
	private static class ProgressHandler extends Handler {

		private WeakReference<ItemsListLoader> wrActivity;

		public ProgressHandler(WeakReference<ItemsListLoader> wrActivity) {

			// Message.show(this);

			this.wrActivity = wrActivity;
		}

		/**
		 * Установка активности, для UI которой хэндлер выполняет действия.
		 * 
		 * @param wrActivity
		 */
		public void setActivity(WeakReference<ItemsListLoader> wrActivity) {

			Message.show(this);

			this.wrActivity = wrActivity;
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			Message.show("[hashCode = " + this.hashCode() + "], what = ["
					+ msg.what + "]");

			// Установка значения прогресса.
			wrActivity.get().pDialog.setProgress(msg.what);
			wrActivity.get().pDialog.setMax(msg.arg1);

			return;
		}
	};

	public ItemsListLoader() {

		rowsCounter = 0;
		rowsTotal = 0;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Message.show(this);

		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this, "Нет экстр у загрузчика строк документа!",
					Toast.LENGTH_LONG).show();
			finish();
		}

		connectionString = extras.getString(CONNECTION_STRING_FIELD_NAME);
		if (connectionString == null) {
			Toast.makeText(
					this,
					"Не указана строка соединения у загрузчика строк документа!",
					Toast.LENGTH_LONG).show();
			finish();
		}

		if (extras != null) {
			docNum = (String) extras.get(DBase.FIELD_DOC_NUM_NAME);
			String docDateString = (String) extras
					.get(DBase.FIELD_DOC_DATE_NAME);
			docDate = Long.valueOf(docDateString);
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

			rowsCounter = savedInstanceState.getInt(FIELD_ROWSCOUNTER_NAME, 0);
			rowsTotal = savedInstanceState.getInt(FIELD_ROWSTOTAL_NAME, 0);
		}

		getSupportLoaderManager().initLoader(ITEMSLIST_LOADER_ID, null, this);
	}

	@Override
	public void onResume() {

		Message.show(this);

		// Инициалиация хэндлера прогресс-диалога.
		if (progressHandler == null) {
			progressHandler = new ProgressHandler(WEAK_REF_ACTIVITY);
		} else {
			progressHandler.setActivity(WEAK_REF_ACTIVITY);
		}

		super.onResume();
	}

	@Override
	public void onPause() {

		super.onPause();

		Message.show(this);

		progressHandler = null;

		// Если загружены не все строки, то результат -1.
		// Если всё, то 1.
		if (rowsCounter == rowsTotal) {
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		outState.putInt(FIELD_ROWSTOTAL_NAME, rowsTotal);
		outState.putInt(FIELD_ROWSCOUNTER_NAME, rowsCounter);
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle bndl) {

		Message.show("[hashCode = " + this.hashCode() + "], id = " + id);

		Loader<Void> ldr = new ItemsAsyncTaskLoader(this);
		Message.show(ldr);

		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void data) {

		Message.show(this, 0);

		setResult(RESULT_OK);
		onCloseDialog(ITEMSLIST_LOADER_ID, BUTTON_BACK_ID);

		// Установка даты обновления.
		Main.setLastItemsListFetchingTime(new GregorianCalendar());
	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {

		Message.show(this, 0);

		setResult(RESULT_CANCELED);
		onCloseDialog(ITEMSLIST_LOADER_ID, BUTTON_BACK_ID);

		// Установка даты обновления.
		Main.setLastItemsListFetchingTime(new GregorianCalendar());
	}

	// /////////////////////////////////////////////////////
	// AsyncTaskLoader<Void>
	// /////////////////////////////////////////////////////
	private static class ItemsAsyncTaskLoader extends AsyncTaskLoader<Void> {

		private Context context;

		private MediaPlayer mp;

		private DBase dBase;

		public ItemsAsyncTaskLoader(Context context) {

			super(context);

			Message.show(this);

			this.context = context;
		}

		@Override
		public void onStartLoading() {

			Message.show(this, 0);

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

				String docDateString = dateFormatter.format(docDate);
				String docId = docDateString.concat(docNum);
				Cursor cursor = dBase.getRowsFiltered(
						DBase.TABLE_ITEMS_DEMO_NAME, DBase.FIELD_DOC_ID_NAME
								+ " = ?", docId, DBase.FIELD_ROW_NUM_NAME);

				// Приходится хранить значения счетчиков в родительской
				// активности, т.к. их нужно будет проверять в событии
				// onPause(), чтобы сделать вывод о том, полностью ли загружены
				// данные.
				WEAK_REF_ACTIVITY.get().setRowsTotal(cursor.getCount());
				WEAK_REF_ACTIVITY.get().setRowsCounter(0);
				cursor.moveToFirst();
				if (cursor.isFirst()) {

					if (!isStarted() || isAbandoned() || isReset())
						return null;

					WEAK_REF_ACTIVITY.get().incrementRowsCounter(1);
					dBase.copyItemRow(DBase.TABLE_ITEMS_NAME, cursor);

					Message.show("[hashCode = " + this.hashCode()
							+ "], copyItemRow("
							+ WEAK_REF_ACTIVITY.get().getRowsCounter() + ")");

					setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(),
							WEAK_REF_ACTIVITY.get().getRowsCounter(),
							WEAK_REF_ACTIVITY.get().getRowsTotal(), 0);

					while (cursor.moveToNext()) {

						try {
							TimeUnit.MILLISECONDS.sleep(DEMO_MODE_SLEEP_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (!isStarted() || isAbandoned() || isReset())
							break;

						WEAK_REF_ACTIVITY.get().incrementRowsCounter(1);
						dBase.copyItemRow(DBase.TABLE_ITEMS_NAME, cursor);

						Message.show("[hashCode = " + this.hashCode()
								+ "], copyItemRow("
								+ WEAK_REF_ACTIVITY.get().getRowsCounter()
								+ ")");

						setProgress(WEAK_REF_ACTIVITY.get()
								.getProgressHandler(), WEAK_REF_ACTIVITY.get()
								.getRowsCounter(), WEAK_REF_ACTIVITY.get()
								.getRowsTotal(), 0);
					}
				}

			} else {

				// Загрузка с сервера.

				// Получение идентификатора устройства.
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String deviceId = tm.getDeviceId();

				// Итоговая строка должна иметь вид:
				// http://express.nsk.ru:9999/eritems.php?deviceid=12345&docdate=20141223120310&docnum="ЭКС00000001"
				// Дата в БД имеет вид миллисекунд с начала Юникс-эпохи,
				// и её следует преобразовать к виду "20140101000000".
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						"yyyyMMddHHmmss", Locale.getDefault());
				String dateURIFormattedString = dateFormatter.format(docDate);
				String query = "?deviceid=" + deviceId + "&docdate="
						+ dateURIFormattedString + "&docnum='";

				// В номере документа может содержаться кириллица, поэтому
				// необходимо преобразование.
				try {
					query = query + URLEncoder.encode(docNum, HTTP.UTF_8) + "'";
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				String uriString = connectionString + query;

				try {

					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();

					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document domDoc = dBuilder.parse(uriString);
					domDoc.getDocumentElement().normalize();

					// Получение всех узлов документов. Т.к. за раз всегда
					// извлекается содержимое только одного документа, то и узел
					// всегда один.
					NodeList docNodes = domDoc
							.getElementsByTagName(DOC_TAG_NAME);

					// Извлечение номера и даты документа.
					String docNumValue = "";
					String docDateTimeValue = "";

					int docsTotal = docNodes.getLength();
					if (docsTotal > 0) {

						Node docNode = docNodes.item(0);

						NamedNodeMap docAttr = docNode.getAttributes();
						Node docNumNode = docAttr
								.getNamedItem(DOC_NUM_TAG_NAME);
						Node docDateTimeNode = docAttr
								.getNamedItem(DOC_DATE_TAG_NAME);

						if (docNumNode != null)
							docNumValue = docNumNode.getTextContent();

						if (docDateTimeNode != null)
							docDateTimeValue = docDateTimeNode.getTextContent();
					}

					// Получение всех узлов строк.
					NodeList rowNodes = domDoc
							.getElementsByTagName(ROW_TAG_NAME);
					rowsTotal = rowNodes.getLength();
					rowsCounter = 0;

					SQLiteDatabase sqliteDb = dBase.getSQLiteDatabase();

					// Перебор всех строк.
					for (int rowIdx = 0; rowIdx < rowsTotal; rowIdx++) {

						if (!isStarted() || isAbandoned() || isReset())
							return null;

						Node rowNode = rowNodes.item(rowIdx);

						// Извлечение номера строки.
						NamedNodeMap rowAttr = rowNode.getAttributes();
						Node rowNumNode = rowAttr
								.getNamedItem(ROW_NUM_TAG_NAME);

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
						String specifDescr = ""; // Наименование характеристики
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
								NamedNodeMap itemAttr = rowFieldNode
										.getAttributes();

								Node itemCodeNode = itemAttr
										.getNamedItem(ITEM_CODE_TAG_NAME);
								itemCode = itemCodeNode.getTextContent();

								// Перебор всех полей номенклатуры.
								NodeList itemFields = rowFieldNode
										.getChildNodes();
								int itemFieldsTotal = itemFields.getLength();
								for (int itemFieldIdx = 0; itemFieldIdx < itemFieldsTotal; itemFieldIdx++) {

									Node itemFieldNode = itemFields
											.item(itemFieldIdx);
									String itemFieldName = itemFieldNode
											.getNodeName();

									if (itemFieldName
											.equals(ITEM_DESCR_TAG_NAME)) {
										itemDescr = itemFieldNode
												.getTextContent();

									} else if (itemFieldName
											.equals(ITEM_DESCR_FULL_TAG_NAME)) {
										itemDescrFull = itemFieldNode
												.getTextContent();
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

								// Получение кода характеристики номенклатуры.
								NamedNodeMap specifAttr = rowFieldNode
										.getAttributes();

								Node specifCodeNode = specifAttr
										.getNamedItem(SPECIF_CODE_TAG_NAME);
								specifCode = specifCodeNode.getTextContent();

								// Перебор всех полей характеристики
								// номенклатуры.
								NodeList specifFields = rowFieldNode
										.getChildNodes();
								int specifFieldsTotal = specifFields
										.getLength();
								for (int specifFieldIdx = 0; specifFieldIdx < specifFieldsTotal; specifFieldIdx++) {

									Node specifFieldNode = specifFields
											.item(specifFieldIdx);
									String specifFieldName = specifFieldNode
											.getNodeName();

									if (specifFieldName
											.equals(SPECIF_DESCR_TAG_NAME)) {
										specifDescr = specifFieldNode
												.getTextContent();
									}
								}

							} else if (rowFieldName.equals(MEASUR_TAG_NAME)) {

								// Перебор всех полей единицы измерения.
								NodeList measurFields = rowFieldNode
										.getChildNodes();
								int measurFieldsTotal = measurFields
										.getLength();
								for (int measurFieldIdx = 0; measurFieldIdx < measurFieldsTotal; measurFieldIdx++) {

									Node measurFieldNode = measurFields
											.item(measurFieldIdx);
									String measurFieldName = measurFieldNode
											.getNodeName();

									if (measurFieldName
											.equals(MEASUR_DESCR_TAG_NAME)) {
										measurDescr = measurFieldNode
												.getTextContent();
									}
								}

							} else if (rowFieldName.equals(PRICE_TAG_NAME)) {

								String priceString = rowFieldNode
										.getTextContent();
								price = Float.parseFloat(priceString.replace(
										",", "."));

							} else if (rowFieldName.equals(QUANT_ACC_TAG_NAME)) {

								String quant_accString = rowFieldNode
										.getTextContent();
								quantAcc = Float.parseFloat(quant_accString
										.replace(",", "."));

							} else if (rowFieldName.equals(QUANT_TAG_NAME)) {

								String quantString = rowFieldNode
										.getTextContent();
								quant = Float.parseFloat(quantString.replace(
										",", "."));
							}
						} // for (int rowFieldIdx = 0; rowFieldIdx <
							// rowFieldsTotal; rowFieldIdx++)

						ContentValues itemValues = new ContentValues();

						// Идентификатор документа состоит из даты документа и
						// его номера.
						itemValues.put(DBase.FIELD_DOC_ID_NAME,
								docDateTimeValue.concat(docNumValue));

						itemValues.put(DBase.FIELD_ROW_NUM_NAME,
								Integer.parseInt(rowNumValue));
						itemValues.put(DBase.FIELD_ITEM_CODE_NAME, itemCode);
						itemValues.put(DBase.FIELD_ITEM_DESCR_NAME, itemDescr);
						itemValues.put(DBase.FIELD_ITEM_DESCR_FULL_NAME,
								itemDescrFull);
						itemValues.put(DBase.FIELD_ITEM_USE_SPECIF_NAME,
								itemUseSpecif);
						itemValues
								.put(DBase.FIELD_SPECIF_CODE_NAME, specifCode);
						itemValues.put(DBase.FIELD_SPECIF_DESCR_NAME,
								specifDescr);
						itemValues.put(DBase.FIELD_MEASUR_DESCR_NAME,
								measurDescr);
						itemValues.put(DBase.FIELD_PRICE_NAME, price);
						itemValues.put(DBase.FIELD_QUANT_ACC_NAME, quantAcc);
						itemValues.put(DBase.FIELD_QUANT_NAME, quant);

						dBase.insert(sqliteDb, DBase.TABLE_ITEMS_NAME,
								itemValues);

						++rowsCounter;

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

			Message.show(this, 0);

			cancelLoad();

			super.onStopLoading();

			onReleaseResources();
		}

		@Override
		public void onCanceled(Void data) {

			Message.show(this, 0);

			super.onCanceled(data);

			onReleaseResources();
		}

		@Override
		protected void onReset() {

			Message.show(this, 0);

			super.onReset();

			stopLoading();

			onReleaseResources();
		}

		@Override
		public void onAbandon() {

			Message.show(this, 0);

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
		private void setProgress(ProgressHandler progressHandler, int what,
				int arg1, int arg2) {

			// Установка состояния индикатора.
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

		onCloseDialog(ITEMSLIST_LOADER_ID, BUTTON_BACK_ID);
	}

	/**
	 * Возвращает ссылку на текущий хендлер.
	 * 
	 * @return
	 */
	public ProgressHandler getProgressHandler() {

		// Message.show(this);

		return progressHandler;
	}

	// Слушатель прогресс-диалога.
	public void onCloseDialog(int dialogId, int buttonId) {

		Message.show("[hashCode = " + this.hashCode() + "dialogId = "
				+ dialogId + ", buttonId = " + buttonId);

		getSupportLoaderManager().getLoader(ITEMSLIST_LOADER_ID).abandon();

		finish();
	}

	/**
	 * @param rowsCounter
	 *            the rowsCounter to set
	 */
	public void setRowsCounter(int rowsCounter) {
		ItemsListLoader.rowsCounter = rowsCounter;
	}

	public int getRowsCounter() {
		return rowsCounter;
	}

	/**
	 * @param rowsCounter
	 *            the rowsCounter to set
	 */
	public void incrementRowsCounter(int increment) {
		ItemsListLoader.rowsCounter += increment;
	}

	/**
	 * @param rowsTotal
	 *            the rowsTotal to set
	 */
	public void setRowsTotal(int rowsTotal) {
		ItemsListLoader.rowsTotal = rowsTotal;
	}

	public int getRowsTotal() {
		return rowsTotal;
	}

	/**
	 * Возвращает дату, отформатированную для использования в URI.<br>
	 * Например, из "01.02.2014" будет возвращено "20140201".
	 * 
	 * @param dateString
	 * @param simpleDateFormat
	 * @return
	 */
	// static String getURIFormattedString(String dateString,
	// SimpleDateFormat simpleDateFormat) {
	//
	// String yearString;
	// String monthOfYearString;
	// String dayOfMonthString;
	// String hoursString;
	// String minutesString;
	// String secondsString;
	//
	// Date date;
	// try {
	//
	// date = simpleDateFormat.parse(dateString);
	//
	// int year = date.getYear() + 1900;
	// int monthOfYear = date.getMonth() + 1;
	// int dayOfMonth = date.getDate();
	// int hours = date.getHours();
	// int minutes = date.getMinutes();
	// int seconds = date.getSeconds();
	//
	// yearString = Integer.toString(year);
	//
	// monthOfYearString = Integer.toString(monthOfYear);
	// if (monthOfYearString.length() < 2) {
	// monthOfYearString = "0" + monthOfYearString;
	// }
	//
	// dayOfMonthString = Integer.toString(dayOfMonth);
	// if (dayOfMonthString.length() < 2) {
	// dayOfMonthString = "0" + dayOfMonthString;
	// }
	//
	// hoursString = Integer.toString(hours);
	// if (hoursString.length() < 2) {
	// hoursString = "0" + hoursString;
	// }
	//
	// minutesString = Integer.toString(minutes);
	// if (minutesString.length() < 2) {
	// minutesString = "0" + minutesString;
	// }
	//
	// secondsString = Integer.toString(seconds);
	// if (secondsString.length() < 2) {
	// secondsString = "0" + secondsString;
	// }
	//
	// } catch (ParseException e) {
	// e.printStackTrace();
	// return "";
	// }
	//
	// return yearString.concat(monthOfYearString).concat(dayOfMonthString)
	// .concat(hoursString).concat(minutesString)
	// .concat(secondsString);
	// }
}
