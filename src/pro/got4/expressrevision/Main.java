package pro.got4.expressrevision;

import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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

	// Имена полей.
	public static final String FIELD_DEMOMODE_NAME = "demoMode";

	public static final int DIALOG_DEMOMODE_ONOFF_ID = 1;
	public static final int DIALOG_DATA_CLEANING_CONFIRMATION = 2;

	public DBase db;

	public static Main main;

	// Флаг демо-режима.
	private static boolean demoMode;

	private Button buttonMain;

	private MediaPlayer mp;

	// Идентификаторы вызываемых активностей.
	// private final int DOCUMENTS_LIST_REQUEST_CODE = 0;

	// Идентификаторы меню параметров.
	private final int OPTIONS_MENU_PREFERENCES_BUTTON_ID = 0;
	private final int OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID = 1;
	private final int OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID = 2;

	/**
	 * Дата последнего получения списка документов.
	 */
	private static GregorianCalendar lastDocsListFetchingTime;

	/**
	 * Дата последнего получения содержимого документа.
	 */
	private static GregorianCalendar lastItemsListFetchingTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Main.main = this;

		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			Main.setDemoMode(savedInstanceState.getBoolean(FIELD_DEMOMODE_NAME));
		}

		setContentView(R.layout.main);

		// открываем подключение к БД
		db = new DBase(this);
		db.open();

		buttonMain = (Button) findViewById(R.id.buttonStart);
		buttonMain.setOnClickListener(this);

		if (savedInstanceState == null) {

			// Проигрывание участка звукового файла.

			// Начало и окончание проигрываемого отрезка.
			final int mStartTime = 0;
			final int mEndTime = 1200;

			mp = MediaPlayer.create(this, R.raw.logo);
			mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {

				Handler mHandler = new Handler();

				// Слушатель процедуры поиска.
				@Override
				public void onSeekComplete(MediaPlayer mp) {
					// Когда завершается процедура поиска, то вызывается это
					// событие.
					// Здесь запускаем плеер и хэндлеру отправляем
					// запускаемый объект с лагом в исполнении на
					// требуемое время.
					mp.start();
					mHandler.postDelayed(mStopAction, mEndTime - mStartTime);
				}

				// Запускаемый объект, который остановит проигрывание и
				// освободит ресурс.
				final Runnable mStopAction = new Runnable() {
					@Override
					public void run() {
						mp.stop();
						mp.release();
					}
				};
			});

			// Переход к проигрыванию отрезка.
			mp.seekTo(mStartTime);
		}
	}

	@Override
	public void onResume() {

		super.onResume();

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
			btnText = btnText + ": " + getLoadedDocumentName();
		}

		Button btn = (Button) findViewById(R.id.buttonStart);
		btn.setText(btnText);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		outState.putBoolean(FIELD_DEMOMODE_NAME, Main.isDemoMode());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem item = menu.add(0, OPTIONS_MENU_PREFERENCES_BUTTON_ID, 0,
				getResources().getString(R.string.settings_title));
		item.setIntent(new Intent(this, Preferences.class));

		menu.add(0, OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID, 1, getResources()
				.getString(R.string.demoMode_on));

		menu.add(0, OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID, 2, getResources()
				.getString(R.string.clearTables));

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Вызывается при закрытии пользовательского диалога.
	 */
	@Override
	public void onCloseCustomDialog(int dialogId, int buttonId) {

		switch (dialogId) {
		case DIALOG_DEMOMODE_ONOFF_ID:

			// В вызванном диалоге включения/выключения деморежима пользователь
			// подтвердил действие, поэтому режим меняется на противоположный.
			if (buttonId == CustomDialogFragment.BUTTON_YES) {

				Main.setDemoMode(!Main.isDemoMode());

				// Включение/выключение демо-режима.
				if (Main.isDemoMode()) {
					switchDemoModeOn();
				} else {
					switchDemoModeOff();
				}
			}
			break;

		case (DIALOG_DATA_CLEANING_CONFIRMATION):

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

				setStartButtonText();
			}

		default:
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// Изменение подписи кнопки меню демо-режима.
		MenuItem item = menu.getItem(OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID);
		item.setTitle(Main.isDemoMode() ? R.string.demoMode_off
				: R.string.demoMode_on);

		// Отключение кнопки очистки таблиц.
		item = menu.getItem(DIALOG_DATA_CLEANING_CONFIRMATION);
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
				Intent intent = new Intent(this, DocsListFragmentActivity.class);
				intent.putExtra(DocsListLoader.CONNECTION_STRING_FIELD_NAME,
						getString(R.string.docsListConnectionString));

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
			// См. onCreateOptionsMenu(Menu menu);
			break;
		}
		case (OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID): {

			boolean rowsExist = true;
			if (db.getRowsCount(DBase.TABLE_DOCS_NAME)
					+ db.getRowsCount(DBase.TABLE_ITEMS_NAME) == 0) {
				rowsExist = false;
			}

			// Отображение диалога о смене режима работы.
			String title = "";
			String message = "";
			if (Main.isDemoMode()) {

				title = getString(R.string.demoModeOffConfirmation);

				if (rowsExist)
					message = getString(R.string.demoModeOffWarning);

			} else {

				title = getString(R.string.demoModeOnConfirmation);

				if (rowsExist)
					message = getString(R.string.demoModeOnWarning);
			}

			CustomDialogFragment.showDialog_YesNo(title, message,
					DIALOG_DEMOMODE_ONOFF_ID, Main.this);

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

			Message.show("requestCode = " + requestCode + ", resultCode = "
					+ resultCode);

			switch (resultCode) {

			case DocsListFragmentActivity.CONTEXTMENU_LOAD_BUTTON_ID: {

				// В контекстном меню списка документов была нажата кнопка
				// загрузки документа.

				// Bundle extras = data.getExtras();
				// String docNum = "";
				// String docDate = "";
				// if (extras != null) {
				// docNum = (String) extras.get(DBase.FIELD_DOC_NUM_NAME);
				//
				// docDate = (String) extras.get(DBase.FIELD_DOC_DATE_NAME);
				// }
				//
				// Message.show("Номер = " + docNum + ", Дата = " + docDate);
				// Toast.makeText(this,
				// "Номер = " + docNum + ", Дата = " + docDate,
				// Toast.LENGTH_LONG).show();

				// Загрузка содержимого демо- или нормального документа (в
				// зависимости от того, в каком режиме находится приложение).
				Intent intent = new Intent(this,
						ItemsListFragmentActivity.class);

				// Строка подключения.
				intent.putExtra(ItemsListLoader.CONNECTION_STRING_FIELD_NAME,
						getString(R.string.itemsListConnectionString));

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
	 * Возвращает имя загруженного документа ревизии.
	 */
	private String getLoadedDocumentName() {
		return "2 киоск: ЭКС000254638 от 12.11.2014";// TODO
	}

	/**
	 * Устанавливает или снимает оформление заднего фона указанного вью
	 * указанной активности в зависимости от того, включён или выключен
	 * демо-режим.
	 * 
	 * @param activity
	 *            - активность, в которой будет произведен поиск вью;
	 * @param viewId
	 *            - вью, задний фон которого следует установить;
	 * @param demoModeOn
	 *            - флаг демо-режима.
	 */
	public static void setStyle(Activity activity) {

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
	 * Включение демо-режима.
	 */
	private void switchDemoModeOn() {

		// Если происходит переключение в демо-режим или обратно, то следует
		// очистить все содержимое таблиц документов и номенклатуры.

		// Установка оформления.
		Main.setDemoMode(true);
		setStyle(this);

		// Очистка таблиц.
		db.clearTable(DBase.TABLE_DOCS_NAME);
		db.clearTable(DBase.TABLE_ITEMS_NAME);

		setStartButtonText();
	}

	/**
	 * Выключение демо-режима.
	 */
	private void switchDemoModeOff() {

		// Если происходит переключение в демо-режим или обратно, то следует
		// очистить все содержимое таблиц документов и номенклатуры.

		Main.setDemoMode(false);
		setStyle(this);

		// Очистка таблиц.
		db.clearTable(DBase.TABLE_DOCS_NAME);
		db.clearTable(DBase.TABLE_ITEMS_NAME);

		setStartButtonText();
	}

	/**
	 * Возвращает флаг необходимости получения нового списка документов с
	 * сервера.
	 */
	public static boolean docsListNeedsToBeFetched() {

		String refreshDocListOnOpening = PreferenceManager
				.getDefaultSharedPreferences(Main.main).getString(
						"refreshDocListOnOpening", "0");

		Integer timeDelay = Integer.valueOf(0);
		if (!refreshDocListOnOpening.isEmpty())
			timeDelay = Integer.parseInt(refreshDocListOnOpening);

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
	 * Возвращает флаг необходимости получения нового содержимого выбранного
	 * документа с сервера.
	 */
	public static boolean itemsListNeedsToBeFetched() {

		return true;
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
	 * @return the lastDocsListFetchingTime
	 */
	public static GregorianCalendar getLastItemsListFetchingTime() {

		GregorianCalendar c = new GregorianCalendar(0, 0, 0);

		if (lastItemsListFetchingTime != null)
			c = (GregorianCalendar) lastItemsListFetchingTime.clone();

		return c;
	}

	/**
	 * @param lastDocsListFetchingTime
	 *            the lastDocsListFetchingTime to set
	 */
	public static void setLastItemsListFetchingTime(GregorianCalendar time) {
		Main.lastItemsListFetchingTime = time;
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
	 * Установка ре
	 * 
	 * @param demoMode
	 *            the demoMode to set
	 */
	private static void setDemoMode(boolean demoMode) {
		Main.demoMode = demoMode;
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
