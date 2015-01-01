package pro.got4.expressrevision;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ItemsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnClickListener {

	public static final int ITEMS_LIST_ID = 2;

	public static final int CONTEXTMENU_LOAD_BUTTON_ID = 1;
	public static final int CONTEXTMENU_LOAD_CANCEL_BUTTON_ID = 2;

	public static final String START_ITEMS_LOADER = "start_items_loader";

	private ListView lvData;

	// private SimpleCursorAdapter scAdapter;
	private ItemsListAdapter adapter;

	public DBase db;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.items_list);

		// открываем подключение к БД
		db = new DBase(this);
		db.open();

		// FIELD_DOC_ID_NAME
		// FIELD_ITEM_USE_SPECIF_NAME
		//
		// FIELD_QUANT_ACC_NAME
		// FIELD_INDEX_NAME
		// формируем столбцы сопоставления
		// String[] from = new String[] {
		// DBase.FIELD_ROW_NUM_NAME,
		// DBase.FIELD_ITEM_CODE_NAME,
		// // DBase.FIELD_ITEM_DESCR_NAME,
		// DBase.FIELD_ITEM_DESCR_FULL_NAME, DBase.FIELD_SPECIF_CODE_NAME,
		// DBase.FIELD_SPECIF_DESCR_NAME, DBase.FIELD_MEASUR_DESCR_NAME,
		// DBase.FIELD_PRICE_NAME, DBase.FIELD_QUANT_NAME };
		// int[] to = new int[] { R.id.row_num_textView,
		// R.id.item_code_textView,
		// // R.id.item_descr_textView,
		// R.id.item_descr_full_textView, R.id.specif_code_textView,
		// R.id.specif_descr_textView, R.id.measur_textView,
		// R.id.price_textView, R.id.quant_button };

		// создаем адаптер и настраиваем список
		// scAdapter = new SimpleCursorAdapter(this,
		// R.layout.items_list_item_specif_4, null, from, to,
		// SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		adapter = new ItemsListAdapter(this, null);
		lvData = (ListView) findViewById(R.id.listViewItems);
		lvData.setAdapter(adapter);

		lvData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});

		// Добавляем контекстное меню к списку.
		// registerForContextMenu(lvData);

		// Загрузчик для чтения данных.
		getSupportLoaderManager().initLoader(ITEMS_LIST_ID, null, this);

		// Если указан флаг начала загрузки, то она начинается.
		// Message.show("[hashCode = " + this.hashCode()
		// + "], getIntent().hashCode() = [" + getIntent().hashCode()
		// + "]");

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

	/**
	 * Обработчик нажатия кнопок.
	 */
	@Override
	public void onClick(View v) {
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);

		// MenuItem menuItem;
		// menuItem = menu.add(0, CONTEXTMENU_LOAD_BUTTON_ID, 0,
		// R.string.btnLoadDocument);
		// menuItem.setCheckable(true);
		// menuItem.setChecked(true);
		//
		// menuItem = menu.add(0, CONTEXTMENU_LOAD_CANCEL_BUTTON_ID, 1,
		// R.string.cancel);

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

		private DBase db;

		private MyCursorLoader(Context context, DBase db) {

			super(context);
			this.db = db;
		}

		@Override
		public Cursor loadInBackground() {

			Cursor cursor = db.getAllRows(DBase.TABLE_ITEMS_NAME);

			return cursor;
		}

		@Override
		public void deliverResult(Cursor cursor) {
			super.deliverResult(cursor);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case ItemsListLoader.ITEMSLIST_LOADER_ID:

			Message.show("requestCode = " + requestCode + ", resultCode = "
					+ resultCode);

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
}
