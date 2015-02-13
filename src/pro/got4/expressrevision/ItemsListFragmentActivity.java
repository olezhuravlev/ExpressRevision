package pro.got4.expressrevision;

import pro.got4.expressrevision.ItemsListAdapter.OnItemButtonClickListener;
import pro.got4.expressrevision.dialogs.NumberInputDialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class ItemsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, TextWatcher, OnKeyListener, OnClickListener,
		OnItemButtonClickListener {

	public static final int ID = 400;

	public static final int CONTEXTMENU_SET_DUPLICATES_AS_VISITED_BUTTON_ID = 401;
	public static final int CONTEXTMENU_SET_DUPLICATES_AS_VISITED_CANCEL_BUTTON_ID = 402;

	private static final String NUMBER_INPUT_DIALOG_TAG = "number_input_dialog";

	public static final String FIELD_SET_DUPLICATES_AS_VISITED_NAME = "setDuplicatesAsVisited";

	public static final String START_ITEMS_LOADER = "start_items_loader";

	// ���� ����, ��� ��� ������� ��������� ��� ����������, ����������
	// ������ ��� ������� ��������, ������������� � ������.
	private static boolean setDuplicatesAsVisited;

	private Button percentButton;
	private EditText itemsFilterEditText;
	private ImageButton clearFilterButton;
	private ListView lvData;

	private ItemsListAdapter adapter;

	public DBase db;

	private int lastLongClickedItemPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.items_list);

		// ��������� �������� ���������� ������������� ����������, ����� ����
		// �������� �����. ��� ����� ��� ����, ����� ������������, �������� �
		// ������ ����� ������� �� ������������, �������� ���������� ����� ��
		// ������ ������, � �� ���������� �� ���� �����.
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// ��������� ����������� � ��
		db = new DBase(this);
		db.open();

		adapter = new ItemsListAdapter(this, null);
		lvData = (ListView) findViewById(R.id.listViewItems);
		lvData.setAdapter(adapter);

		lvData.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				lastLongClickedItemPosition = position;
				return false;
			}
		});

		// ��������� ����������� ���� � ������.
		registerForContextMenu(lvData);

		percentButton = (Button) findViewById(R.id.percentButton);
		percentButton.setOnClickListener(this);

		itemsFilterEditText = (EditText) findViewById(R.id.itemsFilterEditText);
		itemsFilterEditText.addTextChangedListener(this);
		itemsFilterEditText.setOnKeyListener(this);

		clearFilterButton = (ImageButton) findViewById(R.id.clearFilterButton);
		clearFilterButton.setOnClickListener(this);

		// ��������� ��� ������ ������.
		MyCursorLoader myCursorLoader = (MyCursorLoader) getSupportLoaderManager()
				.initLoader(ID, null, this);
		myCursorLoader.setParentalActivity(this);

		// ���� ������ ���� ������ ��������, �� ��� ����������.
		if (getIntent().getExtras().getBoolean(START_ITEMS_LOADER) == true
				&& savedInstanceState == null) {

			// ������� ������� ����������� ���������.
			db.clearTable(DBase.TABLE_ITEMS_NAME);

			Intent intent = new Intent(this, ItemsListLoader.class);

			// ����������� ���������� �������, � ������� ���������� � �.�. �����
			// � ���� ������������ ���������.
			intent.putExtras(getIntent());

			// ������ �������� �����.
			startActivityForResult(intent, ItemsListLoader.ID);

			// ����� �������� ������.
			getSupportLoaderManager().getLoader(ID).forceLoad();
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		setDuplicatesAsVisited = PreferenceManager.getDefaultSharedPreferences(
				this).getBoolean(FIELD_SET_DUPLICATES_AS_VISITED_NAME, true);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);

		// ������� ����� ��������� ����� ������ ��������� �������� ���������
		// ���������� ����������� "����" � ������������ ����� ������� ������ ���
		// � ����������.
		if (hasFocus == true)
			Main.setStyle(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		MenuItem menuItem;
		menuItem = menu.add(0, CONTEXTMENU_SET_DUPLICATES_AS_VISITED_BUTTON_ID,
				0, R.string.btnSetVisitedOff);
		menuItem.setCheckable(true);
		menuItem.setChecked(false);

		menuItem = menu.add(0,
				CONTEXTMENU_SET_DUPLICATES_AS_VISITED_CANCEL_BUTTON_ID, 1,
				R.string.cancel);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == CONTEXTMENU_SET_DUPLICATES_AS_VISITED_BUTTON_ID) {

			// ������ ������� � ��������� �������� ������������.
			Cursor cursor = (Cursor) adapter
					.getItem(lastLongClickedItemPosition);

			int rowIdx = cursor.getColumnIndex(DBase.FIELD_ID_NAME);
			int row_id = cursor.getInt(rowIdx);
			int rowsAffected = setVisitedAttribute(row_id, 0);

			// ����� �������� ������.
			if (rowsAffected > 0)
				getSupportLoaderManager().getLoader(ID).forceLoad();

			return true;

		} else if (item.getItemId() == CONTEXTMENU_SET_DUPLICATES_AS_VISITED_CANCEL_BUTTON_ID) {

			// ������ �� ������. ������ ������ ��� �������, ��� �������� ���
			// ������.
			return true;

		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

		// �.�. ��� ��������� ������������ ���������� ������������ �� �� �����
		// �����������, �� ���� ����� ��� �������,
		// �� �������� ������ ��� �� ��������!
		// db.close();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {

		// ����� ���������� ��� ������ ������ initLoader().
		return new MyCursorLoader(this, db);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		// currentCursor = cursor;
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		adapter.swapCursor(null);
	}

	private static class MyCursorLoader extends CursorLoader {

		ItemsListFragmentActivity parentalActivity;
		private DBase db;

		private MyCursorLoader(Context context, DBase db) {

			super(context);
			this.db = db;
		}

		public void setParentalActivity(Context context) {
			parentalActivity = (ItemsListFragmentActivity) context;
		}

		@Override
		public Cursor loadInBackground() {

			String filter = parentalActivity.getFilter();
			Cursor cursor = db.getRowsFilteredLike(DBase.TABLE_ITEMS_NAME,
					filter);
			return cursor;
		}

		@Override
		public void deliverResult(Cursor cursor) {
			super.deliverResult(cursor);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ItemsListLoader.ID) {

			switch (resultCode) {
			case RESULT_CANCELED: {

				// ���� ��������� �� ��� ������, �� �� ����������� ������
				// ���������, �.�. �������� �.�. �������� ������ ���������, ���
				// �� ���� �������� �����.
				// ��������! ��������� ���������� � ����� ���������� �� ������,
				// ������ ��� �� ����������� ������� � ���� ������ �����������
				// UI!
				DBase dBase = new DBase(this);
				dBase.open();
				int rowsDeleted = dBase.clearTable(DBase.TABLE_ITEMS_NAME);

				String message1 = getString(R.string.loadingCancelled);
				String message2 = message1 + "\n"
						+ getString(R.string.rowsDeleted) + rowsDeleted;

				SpannableString coloredText = new SpannableString(message2);
				coloredText.setSpan(new ForegroundColorSpan(Color.YELLOW), 0,
						message1.length(), 0);

				Toast.makeText(this, coloredText, Toast.LENGTH_LONG).show();

				break;
			}

			case RESULT_OK: {

				DBase dBase = new DBase(this);
				dBase.open();
				long rowsLoaded = dBase.getRowsCount(DBase.TABLE_ITEMS_NAME);

				// ������ ���������� ����������� ����� � ��� �����������,
				// ������� ������� ��� ������������ ���������.
				long assignedRows = getIntent().getExtras().getLong(
						DBase.FIELD_DOC_ROWS_NAME);

				if (rowsLoaded == assignedRows) {

					// ���� ��������� ������ �� ���������� �����, ������� �
					// ���������, �� ������� ���������� ������ ���������, ���
					// ����������� �� ��������� ����������.
					Intent intent = new Intent(this,
							DocsStatusFragmentActivity.class);

					// ����������� ���������� �������, � ������� ����������
					// ����� � ���� ������������ ���������.
					intent.putExtras(getIntent());

					// ������ ���������� ��� ��������� �������.
					String connectionString = PreferenceManager
							.getDefaultSharedPreferences(this)
							.getString(
									DocsListLoader.CONNECTION_STRING_FIELD_NAME,
									"");
					intent.putExtra(
							DocsListLoader.CONNECTION_STRING_FIELD_NAME,
							connectionString);

					// ���������� � ��������� �������, ������ �������� ������
					// �������� �� �������.
					intent.putExtra(
							DocsStatusFragmentActivity.FIELD_STATUS_NAME,
							Integer.parseInt(getString(R.string.docStatusAfterSuccessfulLoading)));

					// COMMAND.set - ��� ������� �� ������� ��������� �������.
					// ������������ ����� �������� �� ������� ��������� �������
					// � ��������� �������� ������� ���������.
					// �.�. ���������, � ������ �� ����� ������ ������ �.�.
					// �������.
					intent.putExtra(
							DocsStatusFragmentActivity.FIELD_COMMAND_NAME,
							DocsStatusFragmentActivity.COMMAND.SET);

					startActivityForResult(intent,
							DocsStatusFragmentActivity.ID);

				} else {

					int rowsDeleted = dBase.clearTable(DBase.TABLE_ITEMS_NAME);

					String message1 = getString(R.string.loadingUnsuccessful);

					String message2 = message1 + "\n"
							+ getString(R.string.rowsDeleted) + rowsDeleted;

					SpannableString coloredText = new SpannableString(message2);
					coloredText.setSpan(new ForegroundColorSpan(Color.RED), 0,
							message1.length(), 0);

					Toast.makeText(this, coloredText, Toast.LENGTH_LONG).show();
				}

				break;
			} // case RESULT_OK: {
			} // switch (resultCode) {

		} else if (requestCode == DocsStatusFragmentActivity.ID) {

			// ������� ����� �� ����������, ��������������� ������ ��������� ��
			// �������.

			DBase dBase = new DBase(this);
			dBase.open();
			String message1;
			String message2;
			SpannableString coloredText;

			switch (resultCode) {
			case RESULT_OK:

				// ������ ������� ����������, ������ ������������� �� �����,
				// ������ ������� ��������� � ����������� �������.

				long rowsLoaded = dBase.getRowsCount(DBase.TABLE_ITEMS_NAME);

				message1 = getString(R.string.documentLoaded);

				message2 = message1 + "\n" + getString(R.string.rowsLoaded)
						+ rowsLoaded;

				coloredText = new SpannableString(message2);
				coloredText.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
						message1.length(), 0);
				break;

			default: {

				// �� ���� ������ ������� ���������, ��� ������ ���������
				// �� ������� ���������� �� �������, � ��� ���������� �
				// ���� ������ �.�. ����������, ��� �������������.
				int rowsDeleted = dBase.clearTable(DBase.TABLE_ITEMS_NAME);

				message1 = getString(R.string.docLoadedButCantSetRevisionStatus);
				message2 = message1 + "\n" + getString(R.string.rowsDeleted)
						+ rowsDeleted;

				coloredText = null;
				coloredText = new SpannableString(message2);
				coloredText.setSpan(new ForegroundColorSpan(Color.RED), 0,
						message1.length(), 0);
			}
			} // switch (resultCode) {

			Toast.makeText(this, coloredText, Toast.LENGTH_LONG).show();

		} // if (requestCode == ItemsListLoader.ITEMSLISTLOADER_ID)

		// ����� �������� ������.
		getSupportLoaderManager().getLoader(ID).forceLoad();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		// ����� �������� ������.
		getSupportLoaderManager().getLoader(ID).forceLoad();
	}

	public String getFilter() {
		return itemsFilterEditText.getText().toString();
	}

	/**
	 * @return the itemsFilterEditText
	 */
	public EditText getItemsFilterEditText() {
		return itemsFilterEditText;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		boolean returnCode = false;

		switch (v.getId()) {

		case R.id.itemsFilterEditText:

			// ����������� ������� ENTER �� ����-����������.
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

				itemsFilterEditText.clearFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						itemsFilterEditText.getWindowToken(), 0);

				returnCode = true; // ��������� ����������.
			}
		}
		return returnCode;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.percentButton:

			String insertStr = "%";

			int offset = itemsFilterEditText.getSelectionStart();
			if (offset < 0)
				offset = 0;

			String currentText = itemsFilterEditText.getText().toString();

			StringBuilder sBuilder = new StringBuilder(currentText);
			sBuilder.insert(offset, insertStr);
			itemsFilterEditText.setText(sBuilder.toString());
			itemsFilterEditText.setSelection(offset + insertStr.length());

			break;

		case R.id.clearFilterButton:

			itemsFilterEditText.setText("");

			break;
		case R.id.quant_button:
			break;
		}
	}

	@Override
	public void onItemButtonClick(View v, Cursor cursor) {

		int row_id_Idx = cursor.getColumnIndex("_id");
		int itemDescrFull_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_DESCR_FULL_NAME);
		int itemUseSpecif_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_USE_SPECIF_NAME);
		int specifDescr_Idx = cursor
				.getColumnIndex(DBase.FIELD_SPECIF_DESCR_NAME);
		int measurDescr_Idx = cursor
				.getColumnIndex(DBase.FIELD_MEASUR_DESCR_NAME);
		int price_Idx = cursor.getColumnIndex(DBase.FIELD_PRICE_NAME);
		int quant_Idx = cursor.getColumnIndex(DBase.FIELD_QUANT_NAME);
		// int itemVisited_Idx =
		// cursor.getColumnIndex(DBase.FIELD_ITEM_VISITED_NAME);

		int row_id = cursor.getInt(row_id_Idx);
		String itemDescrFull = cursor.getString(itemDescrFull_Idx);
		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);
		String specifDescr = cursor.getString(specifDescr_Idx);
		String measurDescr = cursor.getString(measurDescr_Idx);
		float price = cursor.getFloat(price_Idx);
		float quant = cursor.getFloat(quant_Idx);
		// int itemVisited = cursor.getInt(itemVisited_Idx);

		Bundle args = new Bundle();
		args.putInt(NumberInputDialogFragment.ROW_ID_FIELD_NAME, row_id);
		args.putString(NumberInputDialogFragment.TITLE_FIELD_NAME,
				getString(R.string.setQuantity) + " (" + measurDescr + "):");

		String itemDescription = itemDescrFull;
		if (itemUseSpecif != 0) {
			itemDescription = itemDescrFull.concat(" [").concat(specifDescr)
					.concat("]");
		}

		itemDescription = itemDescription.concat(", ")
				.concat(String.valueOf(price)).concat(" ")
				.concat(getString(R.string.currency));

		args.putString(NumberInputDialogFragment.MESSAGE_FIELD_NAME,
				itemDescription);
		args.putFloat(NumberInputDialogFragment.INITIAL_VALUE_FIELD_NAME, quant);

		DialogFragment d = NumberInputDialogFragment.newInstance(args);
		d.show(getSupportFragmentManager(), NUMBER_INPUT_DIALOG_TAG);
	}

	/**
	 * ���������� �� ������� ��� ��������� �������� ����������.
	 * 
	 * @param quantity
	 */
	public void setCurrentQuantity(int row_id, float quantity) {

		// ��������� ���������� ������������� ���������� � �������� ������������
		// ��������.
		ContentValues valuesQuantity = new ContentValues();
		valuesQuantity.put(DBase.FIELD_ITEM_VISITED_NAME, 1);
		valuesQuantity.put(DBase.FIELD_QUANT_NAME, quantity);

		int rowsAffected = 0;

		String whereClause = DBase.FIELD_ID_NAME + " = ?";
		String[] whereArgs = { String.valueOf(row_id) };

		if (setDuplicatesAsVisited) {

			// 1. ��������� ���������� � ��������� ������.
			int rowsAffectedQuantity = db.update(DBase.TABLE_ITEMS_NAME,
					valuesQuantity, whereClause, whereArgs);

			// 2. ��������� �������� ������������ �� ��� ��������� ���������
			// ������.
			int rowsAffectedVisited = setVisitedAttribute(row_id, 1);
			rowsAffected = Math.max(rowsAffectedQuantity, rowsAffectedVisited);

		} else {

			// ���� �������� ��������� ����������� �������� �� �����, ��
			// ���������� ���������������� ������ �� � ������� � ���������
			// ���������� ������������ ��������.
			rowsAffected = db.update(DBase.TABLE_ITEMS_NAME, valuesQuantity,
					whereClause, whereArgs);
		}

		// ����� �������� ������.
		if (rowsAffected > 0)
			getSupportLoaderManager().getLoader(ID).forceLoad();
	}

	/**
	 * ��������� �������� ������������ ����� ��� ���������� �����.
	 * 
	 * @param row_id
	 * @return
	 */
	private int setVisitedAttribute(int row_id, int value) {

		// ���� ����� �������� ��� ��������� ����������� ��������, �� ���
		// ��������� ����������� � ������ �������������� ��� ��� ����, �
		// ����������� �� ����, ������������ �������������� ��� �����������
		// �������� ��� ���.
		// ��� ������������� ������������ �� ����.

		int rowsAffected = 0;

		Cursor cursor = db.getItemRowById(row_id);

		int itemCode_Idx = cursor.getColumnIndex(DBase.FIELD_ITEM_CODE_NAME);
		int itemUseSpecif_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_USE_SPECIF_NAME);

		String itemCode = cursor.getString(itemCode_Idx);
		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);

		String whereClause = null;
		String[] whereArgs = null;
		if (itemUseSpecif == 0) {

			// �������������� �� ������������, ������ ����������
			// ������������ ��� ����� �������������.
			whereClause = DBase.FIELD_ITEM_CODE_NAME + " = ?";
			whereArgs = new String[] { itemCode };

		} else {

			// �������������� ������������ ��� ������� ����������.
			int specifCode_Idx = cursor
					.getColumnIndex(DBase.FIELD_SPECIF_CODE_NAME);
			String specifCode = cursor.getString(specifCode_Idx);

			whereClause = DBase.FIELD_ITEM_CODE_NAME + " = ? AND "
					+ DBase.FIELD_SPECIF_CODE_NAME + " = ?";
			whereArgs = new String[] { itemCode, specifCode };

		}

		ContentValues valuesVisited = new ContentValues();
		valuesVisited.put(DBase.FIELD_ITEM_VISITED_NAME, value);
		rowsAffected = db.update(DBase.TABLE_ITEMS_NAME, valuesVisited,
				whereClause, whereArgs);

		return rowsAffected;
	}
}
