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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.widget.TextView;
import android.widget.Toast;

public class Main extends FragmentActivity implements OnClickListener,
		CustomDialogFragment.OnCloseCustomDialogListener {

	private static final int ID = 50;
	public static final int DIALOG_DATA_CLEANING_CONFIRMATION_ID = 51;
	public static final int DIALOG_UPLOAD_TO_SERVER_CONFIRMATION_ID = 52;

	public static final String FIELD_ORIENTATION_NAME = "displayOrientation";
	public static final String FIELD_DOCS_LIST_OPENING_TIME_DELAY_NAME = "docListOpeningTimeDelay";

	public static final String FIELD_ORIENTATION_AUTO_NAME = "auto";
	public static final String FIELD_ORIENTATION_PORTRAIT_NAME = "portrait";
	public static final String FIELD_ORIENTATION_LANDSCAPE_NAME = "landscape";

	public static final String FIELD_DEMOMODE_NAME = "demoModePrefs";

	public static final int DIALOG_DATA_CLEANING_CONFIRMATION_ITEM_IDX = 1;

	public static DBase db;

	public static Main main;

	// ���� ����-������.
	private static boolean demoMode;

	private Button buttonMain;
	private Button buttonUpload;
	private TextView versionNameTextView;

	private MediaPlayer mp;

	// �������������� ���� ����������.
	private final int OPTIONS_MENU_PREFERENCES_BUTTON_ID = 0;
	private final int OPTIONS_MENU_CLEAR_TABLE_BUTTON_ID = 1;

	/**
	 * ���� ���������� ��������� ������ ����������.
	 */
	private static GregorianCalendar lastDocsListFetchingTime;

	/**
	 * �����-���������, ���� �������� �������� �������� � ���������.
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

		// ��������� �������� �� ���������.
		PreferenceManager.setDefaultValues(this, R.xml.preference, false);

		setContentView(R.layout.main);

		// ��������� ����������� � ��
		db = new DBase(this);
		db.open();

		buttonMain = (Button) findViewById(R.id.buttonStart);
		buttonMain.setOnClickListener(this);

		buttonUpload = (Button) findViewById(R.id.buttonUpload);
		buttonUpload.setOnClickListener(this);

		versionNameTextView = (TextView) findViewById(R.id.versionNameTextView);

		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionNameTextView.setText("ver." + pInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (savedInstanceState == null) {
			mp = MediaPlayer.create(this, R.raw.logo);
			mp.start();
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		// ���� ������ ����� �������� �� ��������, �.�. ����� �.�. ������ ��
		// ������ ������������.
		demoMode = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(FIELD_DEMOMODE_NAME, false);

		setStyle(this);
		setUploadButtonVisibility();
		setStartButtonText();
	}

	private void setStartButtonText() {

		// ���� � �� ��� ���������� ����������� ��������, �� ���������
		// ����������� ���������� � ��� ��������������. ���� ��������� ��� ���,
		// �� ������������ ��� ���������.
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
	 * ���������� ��� �������� ����������������� �������.
	 */
	@Override
	public void onCloseCustomDialog(int dialogId, int buttonId) {

		switch (dialogId) {
		case (DIALOG_DATA_CLEANING_CONFIRMATION_ID): {

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

				// ���� ��������� ������ ���������� ���� ����� ��������.
				setLastDocsListFetchingTime(null);

				setStyle(this);
				setUploadButtonVisibility();
				setStartButtonText();
			}

			break;
		}
		case (DIALOG_UPLOAD_TO_SERVER_CONFIRMATION_ID): {

			// � ��������� ������� ������������� �������� ������ �� ������
			// ������������ ���������� ��������.
			if (buttonId == CustomDialogFragment.BUTTON_YES) {

				// ������ �������� ����������� ���������.
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
			}
		}
		default:
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// ���������� ������ ������� ������.
		MenuItem item = menu
				.getItem(DIALOG_DATA_CLEANING_CONFIRMATION_ITEM_IDX);
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

			// �������� ��� ������ ����-����������, ��� ������ ��������
			// ����������, � ����������� �� ����, ��������� �� ���������� �
			// ����-������.
			if (loadedItems == 0) {

				// �������� ������ ���������� � ��������� ������ �����������,
				// ������� ����� ������������, ���� ���������� �����, ��� ������
				// ������� � ��� ����� ��������� ��������.

				String connectionString = PreferenceManager
						.getDefaultSharedPreferences(this)
						.getString(DocsListLoader.CONNECTION_STRING_FIELD_NAME,
								"");

				Intent intent = new Intent(this, DocsListFragmentActivity.class);
				intent.putExtra(DocsListLoader.CONNECTION_STRING_FIELD_NAME,
						connectionString);

				startActivityForResult(intent, DocsListFragmentActivity.ID);

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

				startActivityForResult(intent, ItemsListFragmentActivity.ID);
			}

			break;
		}
		case R.id.buttonUpload: {

			// ����������� ������� � �������� ������ �� ������.
			CustomDialogFragment.showDialog_YesNo(
					getString(R.string.uploadDataToServer),
					getString(R.string.uploadDataToServerMessage),
					DIALOG_UPLOAD_TO_SERVER_CONFIRMATION_ID, Main.this);
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

			// ����������� ������� �� ������� ������.
			CustomDialogFragment.showDialog_YesNo(
					getString(R.string.dataCleaning),
					getString(R.string.dataCleaningQuestion),
					DIALOG_DATA_CLEANING_CONFIRMATION_ID, Main.this);
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case DocsListFragmentActivity.ID: {

			// ����� ������� �� ����������, ������������ ������ ���������� �
			// ����������� ���������.

			if (resultCode == DocsListFragmentActivity.CONTEXTMENU_LOAD_BUTTON_ID) {

				// � ����������� ���� ������ ���������� ���� ������ ������
				// �������� ���������.

				// �������� ����������� ����- ��� ����������� ��������� (�
				// ����������� �� ����, � ����� ������ ��������� ����������).
				Intent intent = new Intent(this,
						ItemsListFragmentActivity.class);

				// ������ �����������.
				String connectionString = PreferenceManager
						.getDefaultSharedPreferences(this).getString(
								ItemsListLoader.CONNECTION_STRING_FIELD_NAME,
								"");
				intent.putExtra(ItemsListLoader.CONNECTION_STRING_FIELD_NAME,
						connectionString);

				// C������� � ��������� ��������� (���� ���������� �����������,
				// ��������� ���������).
				intent.putExtras(data);

				// ���� ������������� ������� ����������.
				intent.putExtra(ItemsListFragmentActivity.START_ITEMS_LOADER,
						true);

				// ������ ����������, ������������ ������ ������������.
				startActivityForResult(intent, ItemsListFragmentActivity.ID);
			}

			break;
		}
		case ItemsListLoader.ID: {

			// ����� ������� �� ����������, ����������� ���������� ���������
			// �� ������.
			String status = "";
			String messageTitle = "";
			String messageText = "";
			String messageServer = "";
			int messageTitleColor = Color.GREEN;
			int messageServerColor = Color.YELLOW;

			SpannableString coloredText = null;

			// ������ � ��������� �� �������.
			JSONObject jsonObject = null;
			if (data != null) {

				Bundle extras = data.getExtras();
				String jsonResponse = extras
						.getString(ItemsListLoader.FIELD_RESULT_NAME);

				if (jsonResponse != null) {
					try {
						jsonObject = new JSONObject(jsonResponse);
						status = jsonObject
								.getString(ItemsListLoader.FIELD_STATUS_NAME);
						messageServer = jsonObject
								.getString(ItemsListLoader.FIELD_SERVER_MESSAGE_NAME);
					} catch (JSONException e) {
					}
				}
			}

			switch (resultCode) {
			case ItemsListLoader.RESULT_OK: {

				// �������� ��� ��������� �� ������, ��� ��� ��������� ������
				// ��� ���� ���������� ��������������� ������.
				// ���� ������ ������ ��������� � ��������� - ��� �����
				// �������������� ����, ��� ����������� �������� ���������
				// �������.
				// ����� ����� �������� � ���������� ����� �������.
				String requiredStatus = getString(R.string.docStatusAfterSuccessfulUploading);
				if (status.equals(requiredStatus)) {

					DBase dBase = new DBase(this);
					dBase.open();
					int rowsDeleted = dBase.clearTable(DBase.TABLE_ITEMS_NAME);
					messageTitle = getString(R.string.docSuccessfullyUploaded);
					messageText = getString(R.string.rowsDeleted) + rowsDeleted;
					messageServer = "";

				} else {

					// ������ �������� �� ��������� � ���������, ��� �������
					// ���� ��� �������� ���������.
					messageTitle = getString(R.string.docIsNotUploaded);
					messageText = getString(R.string.serverResponse);
					messageServer = getString(R.string.cantSetDocStatus);

					messageTitleColor = Color.RED;
				}

				break;
			}

			case ItemsListLoader.RESULT_CANCELED: {

				// �������� ��������� �� ������ �� �������.
				// ��������� ��������������� ���������.
				messageTitle = getString(R.string.docIsNotUploaded);
				messageText = getString(R.string.serverResponse);

				messageTitleColor = Color.RED;

				break;

			} // case ItemsListLoader.RESULT_CANCELED:
			} // switch (resultCode) {

			if (!messageText.isEmpty()) {
				messageText = "\n" + messageText;
			}

			if (!messageServer.isEmpty()) {
				messageServer = "\n" + messageServer;
			}

			if (messageServer.isEmpty()) {
				messageText = "";
			}

			coloredText = new SpannableString(messageTitle + messageText
					+ messageServer);
			coloredText.setSpan(new ForegroundColorSpan(messageTitleColor), 0,
					messageTitle.length(), 0);
			coloredText.setSpan(new ForegroundColorSpan(messageServerColor),
					messageTitle.length() + messageText.length(),
					messageTitle.length() + messageText.length()
							+ messageServer.length(), 0);

			Toast.makeText(this, coloredText, Toast.LENGTH_LONG).show();

			break;

		} // case ItemsListLoader.ID:
		} // switch (requestCode) {

		setUploadButtonVisibility();
	}

	/**
	 * ������������� ���������, ���������� � ���������� ������� ���� ����������
	 * ��� ��������� ���������� � ������ ����, ������� ��� �������� ����-�����.
	 * 
	 * @param activity
	 *            - ����������, � ������� ����� ���������� ����� ���;
	 * @param viewId
	 *            - ���, ������ ��� �������� ������� ����������;
	 * @param demoModeOn
	 *            - ���� ����-������.
	 */
	public static void setStyle(Activity activity) {

		// ��������� ���������.
		activity.setTitle(getLoadedDocumentDescription(activity));

		// ��������� ����������.
		setOrientation(activity);

		// ��������� ����������.
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
	 * ���������� ������������� ������������ ��������� �������.
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
					.concat(" �� ").concat(docDateString);
		}

		// ���� �������� �� ��������, �� ������������ ������ ��������
		// ����������.
		if (docDescription.isEmpty())
			docDescription = activity.getString(R.string.app_name);

		return docDescription;
	}

	/**
	 * ���������� ������ �� ������ ������ ������� ����������� ����������.</br>
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
	 * ������������� ���������� � ������������ � �����������.
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
	 * ���������� ���� ������������� ��������� ������ ������ ���������� �
	 * �������.
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

		// ���� � ������� ���������� ��������� ������ ������ ������ �������,
		// ��� ������ � ����������, �� ������ ��������� �������� � �������
		// �����.
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
			coloredText.setSpan(
					new ForegroundColorSpan(Color.rgb(100, 170, 230)), 11, 23,
					0);
			Toast.makeText(context, coloredText, Toast.LENGTH_LONG).show();
		}

		return isNetworkAvailable;
	}

	/**
	 * ������������� ��������� ������ �������� �� ������.
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
