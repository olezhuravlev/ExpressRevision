package pro.got4.expressrevision;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ItemsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, TextWatcher, OnKeyListener {

	public static final int ITEMS_LIST_ID = 2;

	public static final int CONTEXTMENU_LOAD_BUTTON_ID = 1;
	public static final int CONTEXTMENU_LOAD_CANCEL_BUTTON_ID = 2;

	public static final String START_ITEMS_LOADER = "start_items_loader";

	private EditText itemsFilterEditText;
	private ListView lvData;

	private ItemsListAdapter adapter;

	public DBase db;

	private Cursor currentCursor;

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

		Message.show("OnCreate, itemsFilterEditText == " + itemsFilterEditText
				+ ", text ==" + itemsFilterEditText.getText().toString());

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
	public void onResume() {

		super.onResume();

		// Стиль, зависящий от режима.
		Main.setStyle(this);
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

		currentCursor = cursor;
		adapter.swapCursor(currentCursor);
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
			Message.show("loadInBackground(), filter = " + filter);
			// Cursor cursor = db.getAllRows(DBase.TABLE_ITEMS_NAME);
			Cursor cursor = db.getFilteredRows(DBase.TABLE_ITEMS_NAME, filter);
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
						+ getString(R.string.rowsDeleted) + " " + rowsDeleted;

				Toast.makeText(this, message, Toast.LENGTH_LONG).show();

			}

			case RESULT_OK: {

				DBase dBase = new DBase(this);
				dBase.open();
				long rowsLoaded = dBase.getRowsCount(DBase.TABLE_ITEMS_NAME);
				String message = getString(R.string.documentLoaded) + "\n"
						+ getString(R.string.rowsLoaded) + " " + rowsLoaded;
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
		Message.show("onTextChanged(), CharSequence = " + s + ", start = "
				+ start + ", before = " + before + ", count = " + count);
	}

	@Override
	public void afterTextChanged(Editable s) {

		String filter = itemsFilterEditText.getText().toString();

		Message.show("afterTextChanged(), filter ==" + filter);

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

		// switch (v.getId()) {
		//
		// case R.id.itemsFilterEditText:
		//
		// // Отлавливаем нажатие ENTER на софт-клавиатуре.
		// if (event.getAction() == KeyEvent.ACTION_UP
		// && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
		//
		// EditText editText = getItemsFilterEditText();
		// editText.clearFocus();
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		// // imm.hideSoftInputFromWindow(editText.getWindowToken(),
		// // InputMethodManager.HIDE_IMPLICIT_ONLY);
		//
		// returnCode = true; // Сообщение обработано.
		// }
		// }
		return returnCode;
	}
}
