package pro.got4.expressrevision;

import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import pro.got4.expressrevision.dialogs.CustomDialogFragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main extends FragmentActivity implements OnClickListener,
		CustomDialogFragment.OnCloseCustomDialogListener {

	public static final String FIELD_ORIENTATION_NAME = "displayOrientation";
	public static final String FIELD_DOCS_LIST_OPENING_TIME_DELAY_NAME = "docListOpeningTimeDelay";

	public static final String FIELD_ORIENTATION_AUTO_NAME = "auto";
	public static final String FIELD_ORIENTATION_PORTRAIT_NAME = "portrait";
	public static final String FIELD_ORIENTATION_LANDSCAPE_NAME = "landscape";

	public static final String FIELD_DEMOMODE_NAME = "demoModePrefs";

	public static final int DIALOG_DATA_CLEANING_CONFIRMATION = 1;

	public static DBase db;

	public static Main main;

	// Флаг демо-режима.
	private static boolean demoMode;

	private Button buttonMain;
	private Button buttonUpload;

	private MediaPlayer mp;

	// Идентификаторы меню параметров.
	private final int OPTIONS_MENU_PREFERENCES_BUTTON_ID = 0;
	private final int OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID = 1;

	/**
	 * Дата последнего получения списка документов.
	 */
	private static GregorianCalendar lastDocsListFetchingTime;

	/**
	 * Класс-контейнер, поля которого содержат сведения о документе.
	 * 
	 */
	public static class DocInfo {
		public String DOC_NUM;
		public long DOC_DATE;
		public String STORE_DESCRIPTION;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Main.main = this;

		super.onCreate(savedInstanceState);

		// if (savedInstanceState != null) {
		//
		// }

		// Установка значений по умолчанию.
		PreferenceManager.setDefaultValues(this, R.xml.preference, false);

		setContentView(R.layout.main);// TODO

		// открываем подключение к БД
		db = new DBase(this);
		db.open();

		buttonMain = (Button) findViewById(R.id.buttonStart);
		buttonMain.setOnClickListener(this);

		buttonUpload = (Button) findViewById(R.id.buttonUpload);
		buttonUpload.setOnClickListener(this);

		if (savedInstanceState == null) {
			mp = MediaPlayer.create(this, R.raw.logo);
			mp.start();
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		// Флаг режима нужно получить из настроек, т.к. вызов м.б. связан со
		// сменой конфигурации.
		demoMode = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(FIELD_DEMOMODE_NAME, false);

		setStyle(this);
		setUploadButtonVisibility();
		setStartButtonText();
	}

	private void setStartButtonText() {

		// Если в БД уже содержится загруженный документ, то выводится
		// приглашение приступить к его редактированию. Если документа еще нет,
		// то предлагается его загрузить.
		String btnText = getString(R.string.btnLoadDocument);

		long loadedItems = db.getRowsCount(DBase.TABLE_ITEMS_NAME);

		if (loadedItems > 0) {
			btnText = getString(R.string.btnEditDocument);
			btnText = btnText + ": " + getLoadedDocumentDescription(this);
		}

		Button btn = (Button) findViewById(R.id.buttonStart);
		btn.setText(btnText);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, OPTIONS_MENU_PREFERENCES_BUTTON_ID, 0, getResources()
				.getString(R.string.settings_title));

		menu.add(0, OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID, 1, getResources()
				.getString(R.string.clearTables));

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Вызывается при закрытии пользовательского диалога.
	 */
	@Override
	public void onCloseCustomDialog(int dialogId, int buttonId) {

		switch (dialogId) {
		case (DIALOG_DATA_CLEANING_CONFIRMATION): {

			// В вызванном диалоге подтверждения очистки данных пользователь
			// подтвердил действие.
			if (buttonId == CustomDialogFragment.BUTTON_YES) {

				// Очистка таблиц.
				db.clearTable(DBase.TABLE_DOCS_NAME);
				if (db.getRowsCount(DBase.TABLE_DOCS_NAME) != 0) {
					Toast.makeText(this, R.string.docsTableNotCleared,
							Toast.LENGTH_LONG).show();
				}

				db.clearTable(DBase.TABLE_ITEMS_NAME);
				if (db.getRowsCount(DBase.TABLE_ITEMS_NAME) != 0) {
					Toast.makeText(this, R.string.itemsTableNotCleared,
							Toast.LENGTH_LONG).show();
				}

				// Дату получения списка документов тоже нужно сбросить.
				setLastDocsListFetchingTime(null);

				setStartButtonText();

				setStyle(this);
			}

			break;
		}
		default:
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// Отключение кнопки очистки таблиц.
		MenuItem item = menu.getItem(DIALOG_DATA_CLEANING_CONFIRMATION);
		boolean rowsExist = true;
		if (db.getRowsCount(DBase.TABLE_DOCS_NAME)
				+ db.getRowsCount(DBase.TABLE_ITEMS_NAME) == 0) {
			rowsExist = false;
		}
		item.setEnabled(rowsExist);
		item.setVisible(rowsExist);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();

		switch (id) {

		case R.id.buttonStart: {

			long loadedItems = db.getRowsCount(DBase.TABLE_ITEMS_NAME);

			// Загрузка или списка демо-документов, или списка реальных
			// документов, в зависимости от того, находится ли приложение в
			// демо-режиме.
			if (loadedItems == 0) {

				// Открытие списка документов с передачей строки подключения,
				// которая может понадобиться, если активность решит, что список
				// устарел и его нужно загрузить повторно.

				String connectionString = PreferenceManager
						.getDefaultSharedPreferences(this)
						.getString(DocsListLoader.CONNECTION_STRING_FIELD_NAME,
								"");

				Intent intent = new Intent(this, DocsListFragmentActivity.class);
				intent.putExtra(DocsListLoader.CONNECTION_STRING_FIELD_NAME,
						connectionString);

				startActivityForResult(intent, DocsListFragmentActivity.ID);

			} else {

				// Загруженные строки документа существуют,
				// следовательно речь идет не о выборе документа для загрузке, а
				// о возвращении к редактированию существующего документа.
				// Поэтому просто открывается список номенклатуры.
				// Загружать ничего не нужно, строка загрузки не нужна.
				Intent intent = new Intent(this,
						ItemsListFragmentActivity.class);
				intent.putExtra(ItemsListFragmentActivity.START_ITEMS_LOADER,
						false);

				startActivityForResult(intent, ItemsListFragmentActivity.ID);
			}

			break;
		}
		case R.id.buttonUpload: {

			// Запуск выгрузки содержимого документа.
			Intent intent = new Intent(this, ItemsListLoader.class);

			String connectionString = PreferenceManager
					.getDefaultSharedPreferences(this)
					.getString(
							ItemsListLoader.CONNECTION_STRING_UPLOAD_PREFS_FIELD_NAME,
							"");
			intent.putExtra(ItemsListLoader.CONNECTION_STRING_FIELD_NAME,
					connectionString);

			DocInfo docInfo = getFirstDocumentInfo(this);
			long loadedItems = db.getRowsCount(DBase.TABLE_ITEMS_NAME);

			if (loadedItems == 0)
				return;

			intent.putExtra(DBase.FIELD_DOC_NUM_NAME, docInfo.DOC_NUM);
			intent.putExtra(DBase.FIELD_DOC_DATE_NAME, docInfo.DOC_DATE);
			intent.putExtra(DBase.FIELD_DOC_ROWS_NAME, loadedItems);
			intent.putExtra(ItemsListLoader.UPLOADING_FLAG_FIELD_NAME, true);
			intent.putExtra(
					ItemsListLoader.FIELD_STATUS_NAME,
					Integer.parseInt(getString(R.string.docStatusAfterSuccessfulUploading)));

			startActivityForResult(intent, ItemsListLoader.ID);

			break;
		}

		default:
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case (OPTIONS_MENU_PREFERENCES_BUTTON_ID): {

			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);

			break;
		}

		case (OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID): {

			// Отображение диалога об очистке таблиц.
			CustomDialogFragment.showDialog_YesNo(
					getString(R.string.dataCleaning),
					getString(R.string.dataCleaningQuestion),
					DIALOG_DATA_CLEANING_CONFIRMATION, Main.this);
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case DocsListFragmentActivity.ID: {

			// Ответ получен из активности, отображающей список документов и
			// загружающей выбранный.

			if (resultCode == DocsListFragmentActivity.CONTEXTMENU_LOAD_BUTTON_ID) {

				// В контекстном меню списка документов была нажата кнопка
				// загрузки документа.

				// Загрузка содержимого демо- или нормального документа (в
				// зависимости от того, в каком режиме находится приложение).
				Intent intent = new Intent(this,
						ItemsListFragmentActivity.class);

				// Строка подключения.
				String connectionString = PreferenceManager
						.getDefaultSharedPreferences(this).getString(
								ItemsListLoader.CONNECTION_STRING_FIELD_NAME,
								"");
				intent.putExtra(ItemsListLoader.CONNECTION_STRING_FIELD_NAME,
						connectionString);

				// Cведения о выбранном документе (были возвращены активностью,
				// вернувшей результат).
				intent.putExtras(data);

				// Флаг необходимости запуска загрузчика.
				intent.putExtra(ItemsListFragmentActivity.START_ITEMS_LOADER,
						true);

				// Запуск активности, отображающей список номенклатуры.
				startActivityForResult(intent, ItemsListFragmentActivity.ID);
			}

			break;
		}
		case ItemsListLoader.ID: {

			// Ответ получен из активности, выгружающей содержимое документа
			// на сервер.

			String messageTitle = "";
			String messageText = "";
			String messageServer = "";
			SpannableString coloredText;

			switch (resultCode) {

			case ItemsListLoader.RESULT_OK: {

				// Документ был успешно отправлен на сервер.
				// Строки документа можно удалить.
				DBase dBase = new DBase(this);
				dBase.open();

				int rowsDeleted = dBase.clearTable(DBase.TABLE_ITEMS_NAME);
				messageTitle = getString(R.string.docSuccessfullyUploaded);
				messageText = "\n" + getString(R.string.rowsDeleted)
						+ rowsDeleted;

				// Сообщение от сервера.
				if (data != null) {

					Bundle extras = data.getExtras();
					String jsonResponse = extras
							.getString(ItemsListLoader.FIELD_RESULT_NAME);

					if (jsonResponse != null) {
						JSONObject jsonObject;
						try {
							jsonObject = new JSONObject(jsonResponse);
							messageServer = jsonObject
									.getString(ItemsListLoader.FIELD_SERVER_MESSAGE_NAME);
						} catch (JSONException e) {
						}
					}
				}

				if (!messageServer.isEmpty()) {
					messageServer = "\n" + getString(R.string.serverResponse)
							+ "\n" + messageServer;
				}

				coloredText = new SpannableString(messageTitle + messageText
						+ messageServer);
				coloredText.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
						messageTitle.length(), 0);
				coloredText.setSpan(new ForegroundColorSpan(Color.YELLOW),
						messageTitle.length() + messageText.length(),
						messageTitle.length() + messageText.length()
								+ messageServer.length(), 0);

				Toast.makeText(this, coloredText, Toast.LENGTH_LONG).show();

				break;
			}

			case ItemsListLoader.RESULT_CANCELED: {

				// Документ отправить на сервер не удалось.
				// Выводится соответствующее сообщение.
				messageTitle = getString(R.string.docIsNotUploaded);
				messageText = "\n" + getString(R.string.serverResponse);

				// Сообщение от сервера.
				if (data != null) {

					Bundle extras = data.getExtras();
					String jsonResponse = extras
							.getString(ItemsListLoader.FIELD_RESULT_NAME);

					if (jsonResponse != null) {
						JSONObject jsonObject;
						try {
							jsonObject = new JSONObject(jsonResponse);
							messageServer = jsonObject
									.getString(ItemsListLoader.FIELD_SERVER_MESSAGE_NAME);
						} catch (JSONException e) {
						}
					}
				}

				if (messageServer.isEmpty()) {
					messageText = "";
				} else {
					messageServer = "\n" + messageServer;
				}

				coloredText = new SpannableString(messageTitle + messageText
						+ messageServer);
				coloredText.setSpan(new ForegroundColorSpan(Color.RED), 0,
						messageTitle.length(), 0);
				coloredText.setSpan(new ForegroundColorSpan(Color.YELLOW),
						messageTitle.length() + messageText.length(),
						messageTitle.length() + messageText.length()
								+ messageServer.length(), 0);

				Toast.makeText(this, coloredText, Toast.LENGTH_LONG).show();

				break;

			} // case ItemsListLoader.RESULT_CANCELED:
			} // switch (resultCode) {

			break;

		} // case ItemsListLoader.ID:
		} // switch (requestCode) {

		setUploadButtonVisibility();
	}

	/**
	 * Устанавливает заголовок, ориентацию и оформление заднего фона указанного
	 * вью указанной активности с учётом того, включён или выключен демо-режим.
	 * 
	 * @param activity
	 *            - активность, в которой будет произведен поиск вью;
	 * @param viewId
	 *            - вью, задний фон которого следует установить;
	 * @param demoModeOn
	 *            - флаг демо-режима.
	 */
	public static void setStyle(Activity activity) {

		// Установка заголовка.
		activity.setTitle(getLoadedDocumentDescription(activity));

		// Установка ориентации.
		setOrientation(activity);

		// Установка оформления.
		Drawable image = null;

		String className = activity.getClass().getSimpleName();
		if (className.equals("Main")) {

			if (Main.isDemoMode()) {

				image = activity.getResources().getDrawable(
						R.drawable.demo_texture);
				image.setAlpha(30);

			} else {
				image = activity.getResources().getDrawable(
						R.drawable.work_texture);
				image.setAlpha(255);

			}

			View v = activity.findViewById(R.id.backgroundLayout);
			v.setBackgroundDrawable(image);

		} else if (className.equals("PreferencesActivity")) {

			if (Main.isDemoMode()) {

				image = activity.getResources().getDrawable(
						R.drawable.demo_texture);
				image.setAlpha(30);

			} else {
				image = activity.getResources().getDrawable(
						R.drawable.work_texture);
				image.setAlpha(255);

			}

			View v = activity.findViewById(android.R.id.list);
			v.setBackgroundDrawable(image);

		} else if (className.equals("DocsListFragmentActivity")) {

			if (Main.isDemoMode()) {

				image = activity.getResources().getDrawable(
						R.drawable.demo_texture);
				image.setAlpha(30);

			} else {
				image = activity.getResources().getDrawable(
						R.drawable.work_texture);
				image.setAlpha(100);

			}

			View v = activity.findViewById(R.id.backgroundLayout);
			v.setBackgroundDrawable(image);

		} else if (className.equals("ItemsListFragmentActivity")) {

			if (Main.isDemoMode()) {

				image = activity.getResources().getDrawable(
						R.drawable.demo_texture);
				image.setAlpha(30);

			} else {
				image = activity.getResources().getDrawable(
						R.drawable.work_texture);
				image.setAlpha(100);

			}

			View v = activity.findViewById(R.id.backgroundLayout);
			v.setBackgroundDrawable(image);
		}
	}

	/**
	 * Возвращает представление загруженного документа ревизии.
	 */
	private static String getLoadedDocumentDescription(Activity activity) {

		DocInfo docInfo = getFirstDocumentInfo(activity);

		String docNum = docInfo.DOC_NUM;
		long docDate = docInfo.DOC_DATE;
		String docDateString = DBase.dateFormatter.format(docDate);
		String storeDescr = docInfo.STORE_DESCRIPTION;

		String docDescription = "";
		if (docNum != null && docDate != 0L && storeDescr != null) {

			docDescription = storeDescr.concat(": ").concat(docNum)
					.concat(" от ").concat(docDateString);
		}

		// Если документ не загружен, то возвращается просто название
		// приложения.
		if (docDescription.isEmpty())
			docDescription = activity.getString(R.string.app_name);

		return docDescription;
	}

	/**
	 * Возвращает курсор на первую строку таблицы загруженных документов.</br>
	 * SELECT</br> Docs.doc_Num AS DocNum,</br> Docs.doc_date AS DocDate,</br>
	 * Docs.store_descr AS Store</br> FROM documents AS Docs</br> INNER JOIN
	 * items AS Items ON Docs.doc_id = Items.doc_id LIMIT 1";
	 * 
	 * @param activity
	 * @return
	 */
	private static DocInfo getFirstDocumentInfo(Activity activity) {

		String sql = "" + "SELECT " + "Docs." + DBase.FIELD_DOC_NUM_NAME
				+ " AS DocNum, " + "Docs." + DBase.FIELD_DOC_DATE_NAME
				+ " AS DocDate, " + "Docs." + DBase.FIELD_STORE_DESCR_NAME
				+ " AS Store FROM " + DBase.TABLE_DOCS_NAME
				+ " AS Docs INNER JOIN " + DBase.TABLE_ITEMS_NAME
				+ " AS Items ON Docs." + DBase.FIELD_DOC_ID_NAME + " = Items."
				+ DBase.FIELD_DOC_ID_NAME + " LIMIT 1";

		Cursor cursor = db.getRows(sql, null);

		DocInfo docInfo = new DocInfo();
		docInfo.DOC_NUM = "";
		docInfo.DOC_DATE = 0;
		docInfo.STORE_DESCRIPTION = "";
		if (cursor.moveToFirst() && cursor.isFirst()) {

			docInfo.DOC_NUM = cursor.getString(0);
			docInfo.DOC_DATE = cursor.getLong(1);
			docInfo.STORE_DESCRIPTION = cursor.getString(2);
		}

		return docInfo;
	}

	/**
	 * Устанавливает ориентацию в соответствии с настройками.
	 */
	private static void setOrientation(Activity activity) {

		String orientationString = PreferenceManager
				.getDefaultSharedPreferences(activity).getString(
						FIELD_ORIENTATION_NAME, FIELD_ORIENTATION_AUTO_NAME);

		int orientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
		if (orientationString.equals(FIELD_ORIENTATION_AUTO_NAME)) {
			orientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
		} else if (orientationString.equals(FIELD_ORIENTATION_PORTRAIT_NAME)) {
			orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		} else if (orientationString.equals(FIELD_ORIENTATION_LANDSCAPE_NAME)) {
			orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		}

		if (activity.getRequestedOrientation() != orientation)
			activity.setRequestedOrientation(orientation);
	}

	/**
	 * Возвращает флаг необходимости получения нового списка документов с
	 * сервера.
	 */
	public static boolean docsListNeedsToBeFetched() {

		String docListOnOpeningTimeDelay = PreferenceManager
				.getDefaultSharedPreferences(Main.main).getString(
						FIELD_DOCS_LIST_OPENING_TIME_DELAY_NAME, "0");

		Integer timeDelay = Integer.valueOf(0);
		if (!docListOnOpeningTimeDelay.isEmpty())
			timeDelay = Integer.parseInt(docListOnOpeningTimeDelay);

		GregorianCalendar expiryTime = Main.getLastDocsListFetchingTime();
		expiryTime.add(GregorianCalendar.SECOND, timeDelay.intValue());

		GregorianCalendar currTime = new GregorianCalendar();

		// Если с момента последнего получения списка прошло больше времени,
		// чем задано в настройках, то список требуется получить с сервера
		// снова.
		if (currTime.after(expiryTime)) {
			return true;
		}

		return false;
	}

	/**
	 * @return the lastDocsListFetchingTime
	 */
	public static GregorianCalendar getLastDocsListFetchingTime() {

		GregorianCalendar c = new GregorianCalendar(0, 0, 0);

		if (lastDocsListFetchingTime != null)
			c = (GregorianCalendar) lastDocsListFetchingTime.clone();

		return c;
	}

	/**
	 * @param lastDocsListFetchingTime
	 *            the lastDocsListFetchingTime to set
	 */
	public static void setLastDocsListFetchingTime(GregorianCalendar time) {
		Main.lastDocsListFetchingTime = time;
	}

	/**
	 * Возвращает флаг установленного демо-режима.
	 * 
	 * @return the demoMode
	 */
	public static boolean isDemoMode() {

		return demoMode;
	}

	/**
	 * Устанавливает флаг демо-режима.
	 * 
	 * @return the demoMode
	 */
	public static void setDemoMode(Activity activity, boolean value) {

		// Установка значения флага в настройках.
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(activity).edit();
		editor.putBoolean(Main.FIELD_DEMOMODE_NAME, value);
		editor.commit();

		demoMode = value;
	}

	/**
	 * Проверка наличия подключения к сети.
	 * 
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context,
			boolean showMessage) {

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();

		boolean isNetworkAvailable = activeNetworkInfo != null
				&& activeNetworkInfo.isConnected();

		if (!isNetworkAvailable && showMessage) {

			SpannableString coloredText = new SpannableString(
					context.getString(R.string.networkNotAvailable));
			coloredText
					.setSpan(new ForegroundColorSpan(Color.rgb(60, 158, 230)),
							11, 23, 0);
			Toast.makeText(context, coloredText, Toast.LENGTH_LONG).show();
		}

		return isNetworkAvailable;
	}

	/**
	 * Устанавливает видимость кнопки выгрузки на сервер.
	 */
	private void setUploadButtonVisibility() {

		long loadedItems = db.getRowsCount(DBase.TABLE_ITEMS_NAME);

		if (loadedItems > 0) {
			buttonUpload.setVisibility(View.VISIBLE);
		} else {
			buttonUpload.setVisibility(View.GONE);
		}
	}
}
