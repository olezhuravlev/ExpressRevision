package pro.got4.expressrevision;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

/**
 * Работа с экраном настроек, созданным на основе XML-описания.
 * 
 * @author programmer
 * 
 */
public class PreferencesActivity extends PreferenceActivity implements
		OnClickListener {

	private static final int ID = 600;

	private static final int DOCS_EDITTEXT_ID = 601;
	private static final int ITEMS_EDITTEXT_ID = 602;

	private static final String DIALOG_TITLE_FIELD_NAME = "title";
	private static final String DIALOG_MESSAGE_FIELD_NAME = "message";

	private static final String CONNECTION_STRING_DOCS_PREFS_ID = "connectionStringDocs";
	private static final String CONNECTION_STRING_ITEMS_PREFS_ID = "connectionStringItems";
	private static final String DEMO_MODE_PREFS_ID = "demoModePrefs";

	private CheckBoxPreference demoModePref;

	private GestureDetector gDetector;

	/**
	 * Доля от ширины ЭУ, над которым производится жест, достаточная для
	 * детектирования этого жеста.
	 */
	private static final int DIVIDER = 4;

	/**
	 * Счетчик жестов, по достижении которого происходит событие.
	 */
	private final int GESTURE_COUNT_ENOUGH = 5;

	private EditTextPreference connectionStringDocsPref;
	private EditText connectionStringDocsPref_EditText;

	private EditTextPreference connectionStringItemsPref;
	private EditText connectionStringItemsPref_EditText;

	/**
	 * ИД элемента, над которым производится жест.
	 */
	private int currentEditText_Id;

	/**
	 * Ширина движения, достаточная для детектирования жеста.
	 */
	private int valuableGestureWidth;

	private int gCounter;

	private float prevDistanceX;
	private float lastTurnPoint_1;
	private float lastTurnPoint_2;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		resetStates();

		gDetector = new GestureDetector(this, new SimpleOnGestureListener() {

			@Override
			public boolean onDown(MotionEvent e) {

				// Если пользователь поднял палец, то все состояния жестов
				// сбрасываются.
				resetStates();

				return false;
			}

			// Детектор жеста, когда пользователь потер пальцем экран.
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {

				// Если текущая дистанция поменяла знак, значит предыдущая точка
				// является поворотной.
				if ((prevDistanceX < 0 && distanceX > 0)
						|| (prevDistanceX > 0 && distanceX < 0)) {

					lastTurnPoint_1 = lastTurnPoint_2;
					lastTurnPoint_2 = e2.getX();

					// Если присутствуют две поворотные точки и расстояние по X
					// между ними превышает значимое расстояние, то считается,
					// что пользователь сдвинул палец достаточно, чтобы
					// зафиксировать жест.
					if (lastTurnPoint_1 != lastTurnPoint_2) {

						float distance = Math.abs(lastTurnPoint_1
								- lastTurnPoint_2);

						if (distance > valuableGestureWidth) {
							++gCounter;
						}

						// Если счетчик превысил требуемое количество жестов, то
						// можно выполнить целевое действие.
						if (gCounter > GESTURE_COUNT_ENOUGH) {

							switch (currentEditText_Id) {
							case DOCS_EDITTEXT_ID:
								connectionStringDocsPref_EditText
										.setText(getString(R.string.docsConnectionString));
							case ITEMS_EDITTEXT_ID:
								connectionStringItemsPref_EditText
										.setText(getString(R.string.itemsConnectionString));
							}

							resetStates();
						}
					}
				}

				prevDistanceX = distanceX;

				return false;
			}
		});

		// Установка вью, в элемент @android:id/list которого будут установлены
		// вью настроек.
		setContentView(R.layout.prefs_listview);

		// Установка детектора жестов для настройки источника получения списка
		// документов.
		connectionStringDocsPref = (EditTextPreference) findPreference(CONNECTION_STRING_DOCS_PREFS_ID);
		connectionStringDocsPref_EditText = connectionStringDocsPref
				.getEditText();
		connectionStringDocsPref_EditText.setId(DOCS_EDITTEXT_ID);
		connectionStringDocsPref_EditText
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						v.performClick();

						currentEditText_Id = v.getId();
						valuableGestureWidth = v.getWidth() / DIVIDER;

						gDetector.onTouchEvent(event);

						return false;
					}
				});

		// Установка детектора жестов для настройки источника получения
		// содержимого документа.
		connectionStringItemsPref = (EditTextPreference) findPreference(CONNECTION_STRING_ITEMS_PREFS_ID);
		connectionStringItemsPref_EditText = connectionStringItemsPref
				.getEditText();
		connectionStringItemsPref_EditText.setId(ITEMS_EDITTEXT_ID);
		connectionStringItemsPref_EditText
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						v.performClick();

						currentEditText_Id = v.getId();
						valuableGestureWidth = v.getWidth() / DIVIDER;

						gDetector.onTouchEvent(event);

						return false;
					}
				});

		// Вызов подтверждения при изменении флажка демо-режима.
		demoModePref = (CheckBoxPreference) findPreference(DEMO_MODE_PREFS_ID);
		demoModePref
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

						showDialog(ID, args);

						return false;
					}
				});
	}

	@Override
	public void onResume() {

		super.onResume();

		Main.setStyle(this);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {

		if (id == ID) {

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
		// очистить все содержимое таблиц документов и номенклатуры, а также
		// сбросить дату получения списка документов.

		// Очистка таблиц.
		Main.db.clearTable(DBase.TABLE_DOCS_NAME);
		Main.db.clearTable(DBase.TABLE_ITEMS_NAME);

		// Сброс даты получения списка документов.
		Main.setLastDocsListFetchingTime(null);

		// Установка флага.
		Main.setDemoMode(this, true);

		demoModePref.setChecked(true);

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

		// Сброс даты получения списка документов.
		Main.setLastDocsListFetchingTime(null);

		// Установка флага.
		Main.setDemoMode(this, false);

		demoModePref.setChecked(false);

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
			removeDialog(ID);

			break;
		}
	}

	/**
	 * Сброс состояний детектора жестов.
	 */
	private void resetStates() {

		currentEditText_Id = 0;

		valuableGestureWidth = 0;

		gCounter = 0;

		prevDistanceX = 0f;
		lastTurnPoint_1 = 0f;
		lastTurnPoint_2 = 0f;
	}
}
