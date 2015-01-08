package pro.got4.expressrevision;

import java.util.GregorianCalendar;

import pro.got4.expressrevision.dialogs.CustomDialogFragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

	public static final String FIELD_ORIENTATION_NAME = "displayOrientation";

	public static final String FIELD_ORIENTATION_AUTO_NAME = "auto";
	public static final String FIELD_ORIENTATION_PORTRAIT_NAME = "portrait";
	public static final String FIELD_ORIENTATION_LANDSCAPE_NAME = "landscape";

	public static final String FIELD_DEMOMODE_NAME = "demoMode";

	public static final int DIALOG_DATA_CLEANING_CONFIRMATION = 1;

	public static final String PREFERENCES_FILE_NAME = "pro.got4.expressrevision";

	public static DBase db;

	public static Main main;

	// ���� ����-������.
	private static boolean demoMode;

	private Button buttonMain;

	private MediaPlayer mp;

	// �������������� ���������� �����������.
	// private final int DOCUMENTS_LIST_REQUEST_CODE = 0;

	// �������������� ���� ����������.
	private final int OPTIONS_MENU_PREFERENCES_BUTTON_ID = 0;
	private final int OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID = 1;

	/**
	 * ���� ���������� ��������� ������ ����������.
	 */
	private static GregorianCalendar lastDocsListFetchingTime;

	/**
	 * ���� ���������� ��������� ����������� ���������.
	 */
	private static GregorianCalendar lastItemsListFetchingTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Main.main = this;

		super.onCreate(savedInstanceState);

		// if (savedInstanceState != null) {
		// Main.setDemoMode(savedInstanceState.getBoolean(FIELD_DEMOMODE_NAME));
		// }

		setContentView(R.layout.main);

		setTitle("Some doc loaded");// TODO ��� ���������?

		// ��������� ����������� � ��
		db = new DBase(this);
		db.open();

		buttonMain = (Button) findViewById(R.id.buttonStart);
		buttonMain.setOnClickListener(this);

		if (savedInstanceState == null) {

			// ������������ ������� ��������� �����.

			// ������ � ��������� �������������� �������.
			final int mStartTime = 0;
			final int mEndTime = 1200;

			mp = MediaPlayer.create(this, R.raw.logo);
			mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {

				Handler mHandler = new Handler();

				// ��������� ��������� ������.
				@Override
				public void onSeekComplete(MediaPlayer mp) {
					// ����� ����������� ��������� ������, �� ���������� ���
					// �������.
					// ����� ��������� ����� � �������� ����������
					// ����������� ������ � ����� � ���������� ��
					// ��������� �����.
					mp.start();
					// mHandler.postDelayed(mStopAction, mEndTime - mStartTime);
				}

				// ����������� ������, ������� ��������� ������������ �
				// ��������� ������.
				final Runnable mStopAction = new Runnable() {
					@Override
					public void run() {
						mp.stop();
						mp.release();
					}
				};
			});

			// ������� � ������������ �������.
			mp.seekTo(mStartTime);
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		// ���� ������ ����� �������� �� ��������, �.�. ����� �.�. ������ ��
		// ������ ������������.
		demoMode = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(FIELD_DEMOMODE_NAME, false);

		setOrientation(this);
		setStyle(this);

		setStartButtonText();

	}

	private void setStartButtonText() {

		// ���� � �� ��� ���������� ����������� ��������, �� ���������
		// ����������� ���������� � ��� ��������������. ���� ��������� ��� ���,
		// �� ������������ ��� ���������.
		String btnText = getString(R.string.btnLoadDocument);

		if (getLoadedItemsCount() > 0) {
			btnText = getString(R.string.btnEditDocument);
			btnText = btnText + ": " + getLoadedDocumentName();
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
	 * ���������� ��� �������� ����������������� �������.
	 */
	@Override
	public void onCloseCustomDialog(int dialogId, int buttonId) {

		switch (dialogId) {
		case (DIALOG_DATA_CLEANING_CONFIRMATION): {
			// � ��������� ������� ������������� ������� ������ ������������
			// ���������� ��������.
			if (buttonId == CustomDialogFragment.BUTTON_YES) {

				// ������� ������.
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

			break;
		}
		default:
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// ��������� ������� ������ ���� ����-������.
		// MenuItem item = menu.getItem(OPTIONS_MENU_DEMO_ON_OFF_BUTTON_ID);
		// item.setTitle(Main.isDemoMode() ? R.string.demoMode_off
		// : R.string.demoMode_on);

		// ���������� ������ ������� ������.
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

			// �������� ��� ������ ����-����������, ��� ������ ��������
			// ����������, � ����������� �� ����, ��������� �� ���������� �
			// ����-������.
			if (loadedItems == 0) {

				// �������� ������ ���������� � ��������� ������ �����������,
				// ������� ����� ������������, ���� ���������� �����, ��� ������
				// ������� � ��� ����� ��������� ��������.
				Intent intent = new Intent(this, DocsListFragmentActivity.class);
				intent.putExtra(DocsListLoader.CONNECTION_STRING_FIELD_NAME,
						getString(R.string.docsListConnectionString));

				startActivityForResult(intent,
						DocsListFragmentActivity.DOCS_LIST_ID);
			} else {

				// ����������� ������ ��������� ����������,
				// ������������� ���� ���� �� � ������ ��������� ��� ��������, �
				// � ����������� � �������������� ������������� ���������.
				// ������� ������ ����������� ������ ������������.
				// ��������� ������ �� �����, ������ �������� �� �����.
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

			// ����������� ������� �� ������� ������.
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

			// ����� ������� �� ����������, ������������ ������ ���������� �
			// ����������� ���������.

			Message.show("requestCode = " + requestCode + ", resultCode = "
					+ resultCode);

			switch (resultCode) {

			case DocsListFragmentActivity.CONTEXTMENU_LOAD_BUTTON_ID: {

				// � ����������� ���� ������ ���������� ���� ������ ������
				// �������� ���������.

				// �������� ����������� ����- ��� ����������� ��������� (�
				// ����������� �� ����, � ����� ������ ��������� ����������).
				Intent intent = new Intent(this,
						ItemsListFragmentActivity.class);

				// ������ �����������.
				intent.putExtra(ItemsListLoader.CONNECTION_STRING_FIELD_NAME,
						getString(R.string.itemsListConnectionString));

				// C������� � ��������� ��������� (���� ���������� �����������,
				// ��������� ���������).
				intent.putExtras(data);

				// ���� ������������� ������� ����������.
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
	 * ���������� ���������� ����� ������������ ���������.
	 */
	private long getLoadedItemsCount() {
		return db.getRowsCount(DBase.TABLE_ITEMS_NAME);
	}

	/**
	 * ���������� ��� ������������ ��������� �������.
	 */
	private String getLoadedDocumentName() {
		return "2 �����: ���000254638 �� 12.11.2014";// TODO ��� ���������?
	}

	/**
	 * ������������� ���������� � ������������ � �����������.
	 */
	public static void setOrientation(Activity activity) {

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
	 * ���������� ���� ������������� ��������� ������ ������ ���������� �
	 * �������.
	 */
	public static boolean docsListNeedsToBeFetched() {

		String docListOnOpeningTimeDelay = PreferenceManager
				.getDefaultSharedPreferences(Main.main).getString(
						"docListOnOpeningTimeDelay", "0");

		Integer timeDelay = Integer.valueOf(0);
		if (!docListOnOpeningTimeDelay.isEmpty())
			timeDelay = Integer.parseInt(docListOnOpeningTimeDelay);

		GregorianCalendar expiryTime = Main.getLastDocsListFetchingTime();
		expiryTime.add(GregorianCalendar.SECOND, timeDelay.intValue());

		GregorianCalendar currTime = new GregorianCalendar();

		// ���� � ������� ���������� ��������� ������ ������ ������ �������,
		// ��� ������ � ����������, �� ������ ��������� �������� � �������
		// �����.
		if (currTime.after(expiryTime)) {
			return true;
		}

		return false;
	}

	/**
	 * ���������� ���� ������������� ��������� ������ ����������� ����������
	 * ��������� � �������.
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
	 * ���������� ���� �������������� ����-������.
	 * 
	 * @return the demoMode
	 */
	public static boolean isDemoMode() {

		return demoMode;
	}

	/**
	 * ������������� ���� ����-������.
	 * 
	 * @return the demoMode
	 */
	public static void setDemoMode(Activity activity, boolean value) {

		// ��������� �������� ����� � ����������.
		// SharedPreferences.Editor editor = activity.getSharedPreferences(
		// PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE).edit();
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(activity).edit();
		editor.putBoolean(Main.FIELD_DEMOMODE_NAME, value);
		editor.commit();

		demoMode = value;
	}

	/**
	 * �������� ������� ����������� � ����.
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
