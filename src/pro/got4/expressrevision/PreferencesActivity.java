package pro.got4.expressrevision;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
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
 * ������ � ������� ��������, ��������� �� ������ XML-��������.
 * 
 * @author programmer
 * 
 */
public class PreferencesActivity extends PreferenceActivity implements
		OnClickListener {

	private static final int DIALOG_DEMOMODE_ONOFF_ID = 1;

	private static final int DOCS_EDITTEXT_ID = 0;
	private static final int ITEMS_EDITTEXT_ID = 1;

	private static final String DIALOG_TITLE_FIELD_NAME = "title";
	private static final String DIALOG_MESSAGE_FIELD_NAME = "message";

	private static final String CONNECTION_STRING_DOCS_PREFS_ID = "connectionStringDocs";
	private static final String CONNECTION_STRING_ITEMS_PREFS_ID = "connectionStringItems";
	private static final String DEMO_MODE_PREFS_ID = "demoModePrefs";

	private CheckBoxPreference demoModePref;

	private GestureDetector gDetector;

	/**
	 * ���� �� ������ ��, ��� ������� ������������ ����, ����������� ���
	 * �������������� ����� �����.
	 */
	private static final int DIVIDER = 4;

	/**
	 * ������� ������, �� ���������� �������� ���������� �������.
	 */
	private final int GESTURE_COUNT_ENOUGH = 5;

	private EditTextPreference connectionStringDocsPref;
	private EditText connectionStringDocsPref_EditText;

	private EditTextPreference connectionStringItemsPref;
	private EditText connectionStringItemsPref_EditText;

	/**
	 * �� ��������, ��� ������� ������������ ����.
	 */
	private int currentEditText_Id;

	/**
	 * ������ ��������, ����������� ��� �������������� �����.
	 */
	private int valuableGestureWidth;

	private int gCounter;

	private float prevDistanceX;
	private float lastTurnPoint_1;
	private float lastTurnPoint_2;

	private static int red, green, blue;

	private OnTouchListener gListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		resetStates();

		red = 255;
		green = 255;
		blue = 255;

		gListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				// ���� ���� ���� �� ������� ��, �� ����� ����� ������� �
				// �������� ���������.
				if (currentEditText != v) {

					red = 255;
					green = 255;
					blue = 255;

					if (currentEditText != null) {
						currentEditText.setBackgroundColor(Color.rgb(red,
								green, blue));
					}

				}

				currentEditText = (EditText) v;
				currentEditText_Width = currentEditText.getWidth();

				gDetector.onTouchEvent(event);

				return false;
			}
		};

		gDetector = new GestureDetector(this, new SimpleOnGestureListener() {

			@Override
			public boolean onDown(MotionEvent e) {

				// ���� ������������ ������ �����, �� ��� ��������� ������
				// ������������.
				resetStates();

				return false;
			}

			// �������� �����, ����� ������������ ����� ������� �����.
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {

				// ���� ������� ��������� �������� ����, ������ ���������� �����
				// �������� ����������.
				if ((prevDistanceX < 0 && distanceX > 0)
						|| (prevDistanceX > 0 && distanceX < 0)) {

					lastTurnPoint_1 = lastTurnPoint_2;
					lastTurnPoint_2 = e2.getX();

					// ���� ������������ ��� ���������� ����� � ���������� �� X
					// ����� ���� ��������� �������� ����������, �� ���������,
					// ��� ������������ ������� ����� ����������, �����
					// ������������� ����.
					if (lastTurnPoint_1 != lastTurnPoint_2) {

						float distance = Math.abs(lastTurnPoint_1
								- lastTurnPoint_2);


						if (distance > valuableGestureWidth) {
							++gCounter;
						}

						// ���� ������� �������� ��������� ���������� ������, ��
						// ����� ��������� ������� ��������.
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

		// ��������� ���, � ������� @android:id/list �������� ����� �����������
		// ��� ��������.
		setContentView(R.layout.prefs_listview);

		// ��������� ��������� ������ ��� ��������� ��������� ��������� ������
		// ����������.
		connectionStringDocsPref = (EditTextPreference) findPreference(CONNECTION_STRING_DOCS_PREFS_ID);
		connectionStringDocsPref_EditText = connectionStringDocsPref
				.getEditText();
		connectionStringDocsPref_EditText.setId(DOCS_EDITTEXT_ID);
		connectionStringDocsPref_EditText
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						currentEditText_Id = v.getId();
						valuableGestureWidth = v.getWidth() / DIVIDER;

						gDetector.onTouchEvent(event);

						return false;
					}
				});

		// ��������� ��������� ������ ��� ��������� ��������� ���������
		// ����������� ���������.
		connectionStringItemsPref = (EditTextPreference) findPreference(CONNECTION_STRING_ITEMS_PREFS_ID);
		connectionStringItemsPref_EditText = connectionStringItemsPref
				.getEditText();
		connectionStringItemsPref_EditText.setId(ITEMS_EDITTEXT_ID);
		connectionStringItemsPref_EditText
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						currentEditText_Id = v.getId();
						valuableGestureWidth = v.getWidth() / DIVIDER;

						gDetector.onTouchEvent(event);

						return false;
					}
				});

		// ����� ������������� ��� ��������� ������ ����-������.
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

						// ����������� ������� � ����� ������ ������.
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

		Main.setStyle(this);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {

		if (id == DIALOG_DEMOMODE_ONOFF_ID) {

			AlertDialog.Builder adb = new AlertDialog.Builder(this);

			// ���������
			String title = args.getString(DIALOG_TITLE_FIELD_NAME);
			adb.setTitle(title);

			// ���������
			String message = args.getString(DIALOG_MESSAGE_FIELD_NAME);
			if (!message.isEmpty())
				adb.setMessage(message);

			// ������ �������������� ������
			adb.setPositiveButton(R.string.yes, this);

			// ������ �������������� ������
			adb.setNegativeButton(R.string.no, this);

			// ������� ������
			return adb.create();
		}

		return super.onCreateDialog(id, args);
	}

	/**
	 * ��������� ����-������.
	 */
	private void switchDemoModeOn() {

		// ���� ���������� ������������ � ����-����� ��� �������, �� �������
		// �������� ��� ���������� ������ ���������� � ������������, � �����
		// �������� ���� ��������� ������ ����������.

		// ������� ������.
		Main.db.clearTable(DBase.TABLE_DOCS_NAME);
		Main.db.clearTable(DBase.TABLE_ITEMS_NAME);

		// ����� ���� ��������� ������ ����������.
		Main.setLastDocsListFetchingTime(null);

		// ��������� �����.
		Main.setDemoMode(this, true);

		demoModePref.setChecked(true);

		Main.setStyle(this);
	}

	/**
	 * ���������� ����-������.
	 */
	private void switchDemoModeOff() {

		// ���� ���������� ������������ � ����-����� ��� �������, �� �������
		// �������� ��� ���������� ������ ���������� � ������������.

		// ������� ������.
		Main.db.clearTable(DBase.TABLE_DOCS_NAME);
		Main.db.clearTable(DBase.TABLE_ITEMS_NAME);

		// ����� ���� ��������� ������ ����������.
		Main.setLastDocsListFetchingTime(null);

		// ��������� �����.
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

			// ����� ������ ������ ��� ��������������.
			removeDialog(DIALOG_DEMOMODE_ONOFF_ID);

			break;
		}
	}

	/**
	 * ����� ��������� ��������� ������.
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
