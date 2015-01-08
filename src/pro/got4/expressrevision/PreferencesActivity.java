package pro.got4.expressrevision;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * Работа с экраном настроек, созданным на основе XML-описания.
 * 
 * @author programmer
 * 
 */
public class PreferencesActivity extends PreferenceActivity implements
		OnClickListener {

	private static final int DIALOG_DEMOMODE_ONOFF_ID = 1;

	private static final String DIALOG_TITLE_FIELD_NAME = "title";
	private static final String DIALOG_MESSAGE_FIELD_NAME = "message";

	private static final String DEMO_MODE_PREFS_ID = "demoModePrefs";
	private CheckBoxPreference demoModePrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		// Установка вью, в элемент @android:id/list которого будут установлены
		// вью настроек.
		setContentView(R.layout.prefs_listview);

		demoModePrefs = (CheckBoxPreference) findPreference(DEMO_MODE_PREFS_ID);
		demoModePrefs
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						boolean rowsExist = true;
						if (Main.db.getRowsCount(DBase.TABLE_DOCS_NAME)
								+ Main.db.getRowsCount(DBase.TABLE_ITEMS_NAME) == 0) {
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

						Bundle args = new Bundle();
						args.putString(DIALOG_TITLE_FIELD_NAME, title);
						args.putString(DIALOG_MESSAGE_FIELD_NAME, message);

						showDialog(DIALOG_DEMOMODE_ONOFF_ID, args);

						return false;
					}
				});
	}

	@Override
	public void onResume() {

		super.onResume();

		Main.setOrientation(this);
		Main.setStyle(this);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {

		if (id == DIALOG_DEMOMODE_ONOFF_ID) {

			AlertDialog.Builder adb = new AlertDialog.Builder(this);

			// заголовок
			String title = args.getString(DIALOG_TITLE_FIELD_NAME);
			adb.setTitle(title);

			// сообщение
			String message = args.getString(DIALOG_MESSAGE_FIELD_NAME);
			if (!message.isEmpty())
				adb.setMessage(message);

			// кнопка положительного ответа
			adb.setPositiveButton(R.string.yes, this);

			// кнопка отрицательного ответа
			adb.setNegativeButton(R.string.no, this);

			// создаем диалог
			return adb.create();
		}

		return super.onCreateDialog(id, args);
	}

	/**
	 * Включение демо-режима.
	 */
	private void switchDemoModeOn() {

		// Если происходит переключение в демо-режим или обратно, то следует
		// очистить все содержимое таблиц документов и номенклатуры.

		// Установка
		// Очистка таблиц.
		Main.db.clearTable(DBase.TABLE_DOCS_NAME);
		Main.db.clearTable(DBase.TABLE_ITEMS_NAME);

		// Установка флага.
		Main.setDemoMode(this, true);

		demoModePrefs.setChecked(true);
		
		Main.setStyle(this);
	}

	/**
	 * Выключение демо-режима.
	 */
	private void switchDemoModeOff() {

		// Если происходит переключение в демо-режим или обратно, то следует
		// очистить все содержимое таблиц документов и номенклатуры.

		// Очистка таблиц.
		Main.db.clearTable(DBase.TABLE_DOCS_NAME);
		Main.db.clearTable(DBase.TABLE_ITEMS_NAME);

		// Установка флага.
		Main.setDemoMode(this, false);

		demoModePrefs.setChecked(false);
		
		Main.setStyle(this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:

			if (Main.isDemoMode()) {
				switchDemoModeOff();
			} else {
				switchDemoModeOn();
			}

			// Чтобы диалог каждый раз пересоздавался.
			removeDialog(DIALOG_DEMOMODE_ONOFF_ID);

			break;
		}
	}
}
