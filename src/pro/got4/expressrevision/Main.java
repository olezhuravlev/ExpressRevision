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
		CustomDialogFragment.OnCloseCustomDialogListener {

	public static final String FIELD_DEMOMODE_NAME = "demoMode";

	public static final int DIALOG_DEMOMODE_ID = 0;

	private final int DOCUMENTS_LIST_REQUEST_CODE = 0;

	private final int OPTIONS_MENU_PREFERENCES_BUTTON_ID = 0;
	private final int OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID = 1;

	private boolean demoMode;

	Button buttonMain;

	FragmentActivity aa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {

			setDemoMode(savedInstanceState.getBoolean(FIELD_DEMOMODE_NAME));

		}

		setContentView(R.layout.main);

		buttonMain = (Button) findViewById(R.id.buttonMain);
		buttonMain.setOnClickListener(this);

		// ���� � �� ��� ���������� ����������� ��������, �� ���������
		// ����������� ���������� � ��� ��������������. ���� ��������� ��� ���,
		// �� ������������ ��� ���������.
		String btnText = getString(R.string.btnLoadDocument);

		if (getLoadedDocumentRowsCount() > 0) {
			btnText = getString(R.string.btnEditDocument);
			btnText = btnText + ": " + getLoadedDocumentName();
		}

		buttonMain.setText(btnText);

		setDemoModeBackground(this, R.id.backgroundLayout, isDemoMode());
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
		prefItem.setIntent(new Intent(this, Preferences.class));

		MenuItem demoItem = menu.add(0, OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID, 1,
				getResources().getString(R.string.demoMode_on));
		demoItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				// ����������� ������� � ����� ������ ������.
				String title = "";
				String message = "";
				if (isDemoMode()) {

					title = getString(R.string.demoModeOffConfirmation);
					message = getString(R.string.demoModeOffWarning);

				} else {

					title = getString(R.string.demoModeOnConfirmation);
					message = getString(R.string.demoModeOnWarning);

				}

				CustomDialogFragment
						.showDialog_YesNo(title, message, Main.this);

				return true; // ������� ����������.
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ���������� ��� �������� ����������������� �������.
	 */
	@Override
	public void onCloseCustomDialog(int id) {

		if (id == CustomDialogFragment.BUTTON_YES) {

			setDemoMode(!isDemoMode());
			setDemoModeBackground(this, R.id.backgroundLayout, isDemoMode());
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// ��������� ������� ������ ����.
		MenuItem item = menu.getItem(OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID);
		item.setTitle(isDemoMode() ? R.string.demoMode_off
				: R.string.demoMode_on);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();

		switch (id) {

		case R.id.buttonMain:
			startActivityForResult(new Intent(this,
					DocsListFragmentActivity.class),
					DOCUMENTS_LIST_REQUEST_CODE);
			break;

		default:
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case DOCUMENTS_LIST_REQUEST_CODE:

			// ����� ������� �� ����������, ������������ ������ ���������� �
			// ����������� ���������.
			Toast.makeText(this, "�������� ��������!", Toast.LENGTH_SHORT)
					.show();

		}

	}

	// �.�. ��������� ���������� �� ������������� � Activity ������������
	// ������, �� ����� �� ����������� showDialog, ����������� Dialog, �
	// ���������� FragmentDialog.
	//
	// public void showDialog() {

	// DialogFragment.show() ����������� � ���������� ��������� �
	// ����������. �� ����� ����� ������� ����� ������������ � ������ ������
	// ������, ������� �������� ����������� ���������� � ������� ��� �����.
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

	// ����� ������� �������� ��������� ��������� ����� ���������
	// setTransition() ����� ��������.
	// ��� ft.commit()???

	// �������� � ����������� �������.
	// DialogFragment newFragment = CustomDialogFragment.getYesNoDialog(
	// "���������", "���������");
	// newFragment.show(ft, "dialog");
	// }

	/**
	 * ���������� ���������� ����� ������������ ���������.
	 */
	private int getLoadedDocumentRowsCount() {
		return 0;
	}

	/**
	 * ���������� ��� ������������ ��������� �������.
	 */
	private String getLoadedDocumentName() {
		return "2 �����: ���000254638 �� 12.11.2014";
	}

	/**
	 * ������������� ��� ������� ���������� ������� ���� ���������� ���
	 * ��������� ���������� � ����������� �� ����, ������� ��� ��������
	 * ����-�����.
	 * 
	 * @param activity
	 *            - ����������, � ������� ����� ���������� ����� ���;
	 * @param viewId
	 *            - ���, ������ ��� �������� ������� ����������;
	 * @param demoModeOn
	 *            - ���� ����-������.
	 */
	public static void setDemoModeBackground(Activity activity, int viewId,
			boolean demoModeOn) {

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

		View v = activity.findViewById(viewId);
		v.setBackgroundDrawable(image);
	}

	// // ���������� �������� ����� ������ showDialog.
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
	// // ���������
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
	// // ������� ������
	// return adb.create();
	// }
	// return super.onCreateDialog(id);
	// }

	/**
	 * ���������� �������� ����� ����-������.
	 * 
	 * @return
	 */
	public boolean isDemoMode() {
		return demoMode;
	}

	/**
	 * ������������� �������� ����� ����-������.
	 * 
	 * @param demoMode
	 */
	public void setDemoMode(boolean demoMode) {
		this.demoMode = demoMode;
	}
}
