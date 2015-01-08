package pro.got4.expressrevision;

import pro.got4.expressrevision.ItemsListAdapter.OnItemButtonClickListener;
import pro.got4.expressrevision.dialogs.NumberInputDialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class ItemsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, TextWatcher, OnKeyListener, OnClickListener,
		OnItemButtonClickListener {

	public static final int ITEMS_LIST_ID = 2;

	public static final int CONTEXTMENU_LOAD_BUTTON_ID = 1;
	public static final int CONTEXTMENU_LOAD_CANCEL_BUTTON_ID = 2;

	private static final String NUMBER_INPUT_DIALOG_TAG = "number_input_dialog";

	public static final String START_ITEMS_LOADER = "start_items_loader";

	private EditText itemsFilterEditText;
	private ImageButton clearFilterButton;
	private ListView lvData;

	private ItemsListAdapter adapter;

	public DBase db;

	// private Cursor currentCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.items_list);

		// Запрещаем экранной клавиатуре автоматически появляться, когда окно
		// получает фокус. Это нужно для того, чтобы пользователь, находясь в
		// режиме ввода фильтра по номенклатуре, повернув устройство видел на
		// экране список, а не клавиатуру во весь экран.
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// открываем подключение к БД
		db = new DBase(this);
		db.open();

		adapter = new ItemsListAdapter(this, null);
		lvData = (ListView) findViewById(R.id.listViewItems);
		lvData.setAdapter(adapter);

		itemsFilterEditText = (EditText) findViewById(R.id.itemsFilterEditText);
		itemsFilterEditText.addTextChangedListener(this);
		itemsFilterEditText.setOnKeyListener(this);

		clearFilterButton = (ImageButton) findViewById(R.id.clearFilterButton);
		clearFilterButton.setOnClickListener(this);

		Message.show();
		// Message.show("OnCreate, itemsFilterEditText == " +
		// itemsFilterEditText
		// + ", text ==" + itemsFilterEditText.getText().toString());

		// Добавляем контекстное меню к списку.
		// registerForContextMenu(lvData);

		// Загрузчик для чтения данных.
		MyCursorLoader myCursorLoader = (MyCursorLoader) getSupportLoaderManager()
				.initLoader(ITEMS_LIST_ID, null, this);
		myCursorLoader.setParentalActivity(this);

		// Если указан флаг начала загрузки, то она начинается.
		if (getIntent().getExtras().getBoolean(START_ITEMS_LOADER) == true
				&& savedInstanceState == null) {

			// Очистка таблицы содержимого документа.
			db.clearTable(DBase.TABLE_ITEMS_NAME);

			Intent intent = new Intent(this, ItemsListLoader.class);

			// Копирование параметров интента, в которых содержится в т.ч. номер
			// и дата загружаемого документа.
			intent.putExtras(getIntent());

			// Запуск загрузки строк.
			startActivityForResult(intent, ItemsListLoader.ITEMSLIST_LOADER_ID);

			// Чтобы обновить список.
			getSupportLoaderManager().getLoader(ITEMS_LIST_ID).forceLoad();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Message.show();
	}

	@Override
	public void onResume() {

		super.onResume();

		Main.setOrientation(this);
		Main.setStyle(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Message.show();
	}

	@Override
	public void onStop() {
		super.onStop();
		Message.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == CONTEXTMENU_LOAD_BUTTON_ID) {

			return true;

		} else if (item.getItemId() == CONTEXTMENU_LOAD_CANCEL_BUTTON_ID) {

			// Ничего не делаем. Кнопка просто для ясности, что нажимать для
			// отмены.
			return true;

		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

		// Т.к. при изменении конфигурации менеджером используется то же самое
		// подключение, то если здесь его закрыть,
		// то добавить записи уже не удасться!
		// db.close();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {

		// Метод вызывается при вызове метода initLoader().
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
			// Message.show("loadInBackground(), filter = " + filter);
			// Cursor cursor = db.getAllRows(DBase.TABLE_ITEMS_NAME);
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

		Message.show("onActivityResult(), requestCode = " + requestCode
				+ ", resultCode = " + resultCode);

		switch (requestCode) {

		case ItemsListLoader.ITEMSLIST_LOADER_ID:

			switch (resultCode) {
			case RESULT_CANCELED: {

				// Если загружены не все строки, то всё загруженные строки
				// удаляются, т.к. документ д.б. загружен только полностью, или
				// не быть загружен везде.
				// ВНИМАНИЕ! Применить транзакцию в самом загрузчике не удаётся,
				// потому что по неизвестной причине в этом случае блокируются
				// UI!
				DBase dBase = new DBase(this);
				dBase.open();
				int rowsDeleted = dBase.clearTable(DBase.TABLE_ITEMS_NAME);
				String message = getString(R.string.documentIsntLoaded) + "\n"
						+ getString(R.string.rowsDeleted) + ": " + rowsDeleted;

				Toast.makeText(this, message, Toast.LENGTH_LONG).show();

			}

			case RESULT_OK: {

				DBase dBase = new DBase(this);
				dBase.open();
				long rowsLoaded = dBase.getRowsCount(DBase.TABLE_ITEMS_NAME);
				String message = getString(R.string.documentLoaded) + "\n"
						+ getString(R.string.rowsLoaded) + ": " + rowsLoaded;
				Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			}

			}
		}

		// Чтобы обновить список.
		getSupportLoaderManager().getLoader(ITEMS_LIST_ID).forceLoad();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// Message.show("beforeTextChanged(), CharSequence = " + s +
		// ", start = "
		// + start + ", count = " + count);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// Message.show("onTextChanged(), CharSequence = " + s + ", start = "
		// + start + ", before = " + before + ", count = " + count);
	}

	@Override
	public void afterTextChanged(Editable s) {

		// String filter = itemsFilterEditText.getText().toString();
		// Message.show("afterTextChanged(), filter ==" + filter);

		// Чтобы обновить список.
		getSupportLoaderManager().getLoader(ITEMS_LIST_ID).forceLoad();
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

		Message.show("onKey(), keyCode ==" + keyCode + ", event ==" + event);

		boolean returnCode = false;

		switch (v.getId()) {

		case R.id.itemsFilterEditText:

			// Отлавливаем нажатие ENTER на софт-клавиатуре.
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

				Message.show("onKey(), ACTION_DOWN && KEYCODE_ENTER");

				itemsFilterEditText.clearFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						itemsFilterEditText.getWindowToken(), 0);

				returnCode = true; // Сообщение обработано.
			}
		}
		return returnCode;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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

		int row_id = cursor.getInt(row_id_Idx);
		String itemDescrFull = cursor.getString(itemDescrFull_Idx);
		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);
		String specifDescr = cursor.getString(specifDescr_Idx);
		String measurDescr = cursor.getString(measurDescr_Idx);
		float price = cursor.getFloat(price_Idx);
		float quant = cursor.getFloat(quant_Idx);

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
	 * Вызывается из диалога для установки значения количества.
	 * 
	 * @param quantity
	 */
	public void setCurrentQuantity(int row_id, float quantity) {

		ContentValues values = new ContentValues();
		values.put(DBase.FIELD_QUANT_NAME, quantity);
		db.update(db.getSQLiteDatabase(), DBase.TABLE_ITEMS_NAME, row_id,
				values);

		// Чтобы обновить список.
		getSupportLoaderManager().getLoader(ITEMS_LIST_ID).forceLoad();
	}
}
