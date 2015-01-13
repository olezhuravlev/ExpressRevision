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
 * ������ � ������� ��������, ��������� �� ������ XML-��������.
 * 
 * @author programmer
 * 
 */
public class PreferencesActivity extends PreferenceActivity implements
		OnClickListener {

	private static final int DIALOG_DEMOMODE_ONOFF_ID = 1;

	private static final String DIALOG_TITLE_FIELD_NAME = "title";
	private static final String DIALOG_MESSAGE_FIELD_NAME = "message";

	private static final String CONNECTION_STRING_DOCS_PREFS_ID = "connectionStringDocs";
	private static final String CONNECTION_STRING_ITEMS_PREFS_ID = "connectionStringItems";
	private static final String DEMO_MODE_PREFS_ID = "demoModePrefs";

	private EditTextPreference connectionStringDocs;
	private EditTextPreference connectionStringItems;
	private CheckBoxPreference demoModePrefs;

	// �������, ��� ������� ������������ ����.
	private EditText currentEditText;

	// ������ ��������, ��� ������� ������������ ����.
	int currentEditText_Width;

	private EditText connectionStringDocs_EditText;
	private EditText connectionStringItems_EditText;

	private GestureDetector gDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		currentEditText = null;
		currentEditText_Width = 0;

		gDetector = new GestureDetector(this, new SimpleOnGestureListener() {

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {

				int gestureWidth = Math.abs((int) (e2.getX() - e1.getX()));
				int significantMove = currentEditText_Width / 4;

				if (significantMove > 0 && gestureWidth >= significantMove
						&& Math.abs(velocityX) > Math.abs(velocityY)) {

					Tracker.show("gestureWidth = " + gestureWidth
							+ ", currentEditText_Width = "
							+ currentEditText_Width);

					int color = 0xFFFF8578;
					currentEditText.setBackgroundColor(color);
				}

				currentEditText = null;
				currentEditText_Width = 0;

				return false;
			}
		});

		// ��������� ���, � ������� @android:id/list �������� ����� �����������
		// ��� ��������.
		setContentView(R.layout.prefs_listview);

		// ��������� ��������� ������ ��� ��������� ��������� ��������� ������
		// ����������.
		connectionStringDocs = (EditTextPreference) findPreference(CONNECTION_STRING_DOCS_PREFS_ID);
		connectionStringDocs_EditText = connectionStringDocs.getEditText();
		connectionStringDocs_EditText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				currentEditText = (EditText) v;
				currentEditText_Width = currentEditText.getWidth();

				gDetector.onTouchEvent(event);

				return false;
			}
		});

		// ��������� ��������� ������ ��� ��������� ��������� ���������
		// ����������� ���������.
		connectionStringItems = (EditTextPreference) findPreference(CONNECTION_STRING_ITEMS_PREFS_ID);
		connectionStringItems_EditText = connectionStringItems.getEditText();
		connectionStringItems_EditText
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						currentEditText = (EditText) v;
						currentEditText_Width = currentEditText.getWidth();

						gDetector.onTouchEvent(event);

						return false;
					}
				});

		// ����� ������������� ��� ��������� ������ ����-������.
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

		demoModePrefs.setChecked(true);

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

			// ����� ������ ������ ��� ��������������.
			removeDialog(DIALOG_DEMOMODE_ONOFF_ID);

			break;
		}
	}
}
