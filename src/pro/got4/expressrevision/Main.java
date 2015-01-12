package pro.got4.expressrevision;

import java.util.GregorianCalendar;

import pro.got4.expressrevision.dialogs.CustomDialogFragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
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

	private MediaPlayer mp;

	// Идентификаторы меню параметров.
	private final int OPTIONS_MENU_PREFERENCES_BUTTON_ID = 0;
	private final int OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID = 1;

	/**
	 * Дата последнего получения списка документов.
	 */
	private static GregorianCalendar lastDocsListFetchingTime;

	/**
	 * Дата последнего получения содержимого документа.
	 */
	// private static GregorianCalendar lastItemsListFetchingTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Main.main = this;

		super.onCreate(savedInstanceState);

		// if (savedInstanceState != null) {
		//
		// }

		// Установка значений по умолчанию.
		PreferenceManager.setDefaultValues(this, R.xml.preference, false);

		setContentView(R.layout.main);

		// открываем подключение к БД
		db = new DBase(this);
		db.open();

		buttonMain = (Button) findViewById(R.id.buttonStart);
		buttonMain.setOnClickListener(this);

		if (savedInstanceState == null) {

			// Проигрывание участка звукового файла.

			// Начало и окончание проигрываемого отрезка.
			// final int mStartTime = 0;
			// final int mEndTime = 1200;

			mp = MediaPlayer.create(this, R.raw.logo);
			mp.start();
			// mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			//
			// Handler mHandler = new Handler();
			//
			// // Слушатель процедуры поиска.
			// @Override
			// public void onSeekComplete(MediaPlayer mp) {
			// // Когда завершается процедура поиска, то вызывается это
			// // событие.
			// // Здесь запускаем плеер и хэндлеру отправляем
			// // запускаемый объект с лагом в исполнении на
			// // требуемое время.
			// mp.start();
			// // mHandler.postDelayed(mStopAction, mEndTime - mStartTime);
			// }
			//
			// // Запускаемый объект, который остановит проигрывание и
			// // освободит ресурс.
			// final Runnable mStopAction = new Runnable() {
			// @Override
			// public void run() {
			// mp.stop();
			// mp.release();
			// }
			// };
			// });

			// Переход к проигрыванию отрезка.
			// mp.seekTo(mStartTime);
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

		setStartButtonText();
	}

	private void setStartButtonText() {

		// Если в БД уже содержится загруженный документ, то выводится
		// приглашение приступить к его редактированию. Если документа еще нет,
		// то предлагается его загрузить.
		String btnText = getString(R.string.btnLoadDocument);

		if (getLoadedItemsCount() > 0) {
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

		case R.id.buttonStart:

			long loadedItems = getLoadedItemsCount();

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

				startActivityForResult(intent,
						DocsListFragmentActivity.DOCS_LIST_ID);
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

				startActivityForResult(intent,
						ItemsListFragmentActivity.ITEMS_LIST_ID);
			}

			break;

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

		case DocsListFragmentActivity.DOCS_LIST_ID: {

			// Ответ получен из активности, отображающей список документов и
			// загружающей выбранный.

			switch (resultCode) {

			case DocsListFragmentActivity.CONTEXTMENU_LOAD_BUTTON_ID: {

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

				startActivityForResult(intent,
						ItemsListFragmentActivity.ITEMS_LIST_ID);

				break;
			}
			}
		}
			break;
		}
	}

	/**
	 * Возвращает количество строк загруженного документа.
	 */
	private long getLoadedItemsCount() {
		return db.getRowsCount(DBase.TABLE_ITEMS_NAME);
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

			View v = activity.findViewById(R.id.backgroundLayout_main);
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

		// Извлекается первая запись из таблицы загруженной номенклатуры.
		// Из этой записи по идентификатору документа извлекается документ из
		// таблицы загруженных документов.
		// Из строки документа извлекается его номер, дата и описание склада.
		String sql = "" + "SELECT " + "Docs." + DBase.FIELD_DOC_NUM_NAME
				+ " AS DocNum, " + "Docs." + DBase.FIELD_DOC_DATE_NAME
				+ " AS DocDate, " + "Docs." + DBase.FIELD_STORE_DESCR_NAME
				+ " AS Store FROM " + DBase.TABLE_DOCS_NAME
				+ " AS Docs INNER JOIN " + DBase.TABLE_ITEMS_NAME
				+ " AS Items ON Docs." + DBase.FIELD_DOC_ID_NAME + " = Items."
				+ DBase.FIELD_DOC_ID_NAME + " LIMIT 1";

		Cursor cursor = db.getRows(sql, null);
		String docDescription = "";
		if (cursor.moveToFirst() && cursor.isFirst()) {
			// cursor.getCount();

			String num = cursor.getString(0);
			long date = cursor.getLong(1);
			String docDateString = DBase.dateFormatter.format(date);
			String store = cursor.getString(2);

			docDescription = store.concat(": ").concat(num).concat(" от ")
					.concat(docDateString);
		}

		// Если документ не загружен, то возвращается просто название
		// приложения.
		if (docDescription.isEmpty())
			docDescription = activity.getString(R.string.app_name);

		return docDescription;
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

	// /**
	// * Возвращает флаг необходимости получения нового содержимого выбранного
	// * документа с сервера.
	// */
	// public static boolean itemsListNeedsToBeFetched() {
	// return true;
	// }

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

	// /**
	// * @return the lastDocsListFetchingTime
	// */
	// public static GregorianCalendar getLastItemsListFetchingTime() {
	//
	// GregorianCalendar c = new GregorianCalendar(0, 0, 0);
	//
	// if (lastItemsListFetchingTime != null)
	// c = (GregorianCalendar) lastItemsListFetchingTime.clone();
	//
	// return c;
	// }
	//
	// /**
	// * @param lastDocsListFetchingTime
	// * the lastDocsListFetchingTime to set
	// */
	// public static void setLastItemsListFetchingTime(GregorianCalendar time) {
	// Main.lastItemsListFetchingTime = time;
	// }

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
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
