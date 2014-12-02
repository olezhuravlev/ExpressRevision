package pro.got4.expressrevision;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main extends FragmentActivity implements OnClickListener,
		CustomDialogFragment.OnCloseDialogListener {

	public static final String FIELD_DEMOMODE_NAME = "demoMode";

	public static final int DIALOG_DEMOMODE_ID = 0;

	private final int DOCUMENTS_LIST_REQUEST_CODE = 0;

	private final int OPTIONS_MENU_PREFERENCES_BUTTON_ID = 0;
	private final int OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID = 1;

	private boolean demoMode;

	Button buttonMain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {

			setDemoMode(savedInstanceState.getBoolean(FIELD_DEMOMODE_NAME));

		}

		setContentView(R.layout.main);

		buttonMain = (Button) findViewById(R.id.buttonMain);
		buttonMain.setOnClickListener(this);

		// Если в БД уже содержится загруженный документ, то выводится
		// приглашение приступить к его редактированию. Если документа еще нет,
		// то предлагается его загрузить.
		String btnText = getString(R.string.btnLoadDocument);

		if (getLoadedDocumentRowsCount() > 0) {
			btnText = getString(R.string.btnEditDocument);
			btnText = btnText + ": " + getLoadedDocumentName();
		}

		buttonMain.setText(btnText);

		setDemoModeBackground(this, isDemoMode(), R.id.backgroundLayout);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		outState.putBoolean(FIELD_DEMOMODE_NAME, isDemoMode());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem prefItem = menu.add(0, OPTIONS_MENU_PREFERENCES_BUTTON_ID, 0,
				getResources().getString(R.string.settings_title));
		prefItem.setIntent(new Intent(this, PrefActivity.class));

		MenuItem demoItem = menu.add(0, OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID, 1,
				getResources().getString(R.string.demoMode_on));
		demoItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				// Отображение диалога о смене режима работы.
				CustomDialogFragment.showDialog_YesNo(
						getString(R.string.demoModeOnConfirmation),
						getString(R.string.demoModeOnWarning), Main.this);

				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCloseDialog(int id) {

		String message = "";

		if (id == CustomDialogFragment.BUTTON_YES)
			message = "Yes!";
		else
			message = "No!";

		Toast.makeText(Main.this, "onCloseDialog: " + message,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// Изменение подписи кнопки меню.
		MenuItem item = menu.getItem(OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID);
		item.setTitle(isDemoMode() ? R.string.demoMode_off
				: R.string.demoMode_on);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onClick(View v) {
		startActivityForResult(
				new Intent(this, DocsListFragmentActivity.class),
				DOCUMENTS_LIST_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == DOCUMENTS_LIST_REQUEST_CODE) {

			// Ответ получен из активности, отображающей список документов и
			// загружающей выбранный.
			Toast.makeText(this, "Документ загружен!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// Т.к. сигнатура отличается от определенного в Activity одноименного
	// метода, то метод не перегружает showDialog, вызывающего Dialog, а
	// использует FragmentDialog.
	//
	// public void showDialog() {

	// DialogFragment.show() позаботится о добавлении фрагмента в
	// транзакцию. Мы также хотим удалять любой отображаемый в данный момент
	// диалог, поэтому создадим собственную транзакцию и сделаем это здесь.
	// FragmentManager - Interface for interacting with Fragment objects inside
	// of an Activity
	// FragmentManager fm = getSupportFragmentManager();

	// FragmentTransaction - API for performing a set of Fragment operations.
	// FragmentTransaction ft = fm.beginTransaction();

	// Fragment prev = fm.findFragmentByTag("dialog");
	// if (prev != null) {
	// ft.remove(prev);
	// }
	// ft.addToBackStack(null);

	// можно сделать анимацию появления фрагмента путем установки
	// setTransition() перед коммитом.
	// ГДЕ ft.commit()???

	// Создание и отображение диалога.
	// DialogFragment newFragment = CustomDialogFragment.getYesNoDialog(
	// "Заголовок", "Сообщение");
	// newFragment.show(ft, "dialog");
	// }

	/**
	 * Возвращает количество строк загруженного документа.
	 */
	private int getLoadedDocumentRowsCount() {
		return 0;
	}

	/**
	 * Возвращает имя загруженного документа ревизии.
	 */
	private String getLoadedDocumentName() {
		return "2 киоск: ЭКС000254638 от 12.11.2014";
	}

	/**
	 * Устанавливает/снимает оформление заднего фона в демо/обычном режиме.
	 * 
	 */
	public static void setDemoModeBackground(Activity activity,
			boolean demoModeOn, int viewId) {

		View tl = activity.findViewById(viewId);

		Drawable image = null;

		if (demoModeOn) {

			image = activity.getResources()
					.getDrawable(R.drawable.demo_texture);
			image.setAlpha(50);

		} else {
			image = activity.getResources()
					.getDrawable(R.drawable.work_texture);
			image.setAlpha(255);

		}

		tl.setBackgroundDrawable(image);
	}

	// // Вызывается системой после вызова showDialog.
	// @Override
	// public Dialog onCreateDialog(int id) {
	//
	// if (id == DIALOG_DEMOMODE_ID) {
	//
	// AlertDialog.Builder adb = new AlertDialog.Builder(this);
	//
	// System.out.println("onCreateDialog:demoMode = " + isDemoMode());
	//
	// adb.setTitle(isDemoMode() ? R.string.demoModeOffConfirmation
	// : R.string.demoModeOnConfirmation);
	//
	// // сообщение
	// adb.setMessage(isDemoMode() ? R.string.demoModeOffWarning
	// : R.string.demoModeOnWarning);
	//
	// adb.setIcon(android.R.drawable.ic_dialog_alert);
	// adb.setPositiveButton(R.string.yes,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// setDemoMode(!isDemoMode());
	// setDemoModeBackground(Main.this, isDemoMode(),
	// R.id.backgroundLayout);
	// }
	// });
	//
	// adb.setNegativeButton(R.string.no,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// }
	// });
	//
	// // создаем диалог
	// return adb.create();
	// }
	// return super.onCreateDialog(id);
	// }

	/**
	 * Возвращает значение флага демо-режима.
	 * 
	 * @return
	 */
	public boolean isDemoMode() {
		return demoMode;
	}

	/**
	 * Устанавливает значение флага демо-режима.
	 * 
	 * @param demoMode
	 */
	public void setDemoMode(boolean demoMode) {
		this.demoMode = demoMode;
	}
}
