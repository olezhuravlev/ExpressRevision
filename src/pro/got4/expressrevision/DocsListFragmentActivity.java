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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

public class DocsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnClickListener {

	public static final int DOCS_LIST_ID = 1;

	public static final int CONTEXTMENU_LOAD_BUTTON_ID = 1;
	public static final int CONTEXTMENU_LOAD_CANCEL_BUTTON_ID = 2;

	private ListView lvData;

	// private SimpleCursorAdapter scAdapter;
	private DocsListAdapter adapter;

	private Button buttonLoad;

	public DBase db;

	private int lastLongClickedItemPosition;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.docs_list);

		// открываем подключение к БД
		db = new DBase(this);
		db.open();

		buttonLoad = (Button) findViewById(R.id.buttonLoad);
		buttonLoad.setOnClickListener(this);

		// формируем столбцы сопоставления
		// String[] from = new String[] { DBase.FIELD_STORE_DESCR_NAME,
		// DBase.FIELD_DOC_DATE_NAME, DBase.FIELD_DOC_NUM_NAME,
		// DBase.FIELD_DOC_COMMENT_NAME };
		// int[] to = new int[] { R.id.storeTextView, R.id.dateTextView,
		// R.id.numberTextView, R.id.commentTextView };

		// создаем адаптер и настраиваем список
		// scAdapter = new SimpleCursorAdapter(this, R.layout.docs_list_item,
		// null, from, to,
		// SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		adapter = new DocsListAdapter(this, null);
		lvData = (ListView) findViewById(R.id.docsListView);
		lvData.setAdapter(adapter);

		lvData.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				lastLongClickedItemPosition = position;
				return false;
			}
		});

		// Добавляем контекстное меню к списку.
		registerForContextMenu(lvData);

		// Загрузчик для чтения данных.
		getSupportLoaderManager().initLoader(DOCS_LIST_ID, null, this);

		// Если существует необходимость обновления списка документов, то
		// получаем его с сервера повторно.
		// Здесь следует проверять, не является ли запуск данной активности
		// перезапуском вследствие изменения конфигурации (savedInstanceState ==
		// null). В противном случае,если запустить получение с сервера и в
		// процессе его поменять конфигурацию, то загрузка после выполнения
		// будет немедленно запущена снова!
		if (Main.docsListNeedsToBeFetched() && savedInstanceState == null) {

			// Очистка таблиц.
			db.clearTable(DBase.TABLE_DOCS_NAME);
			db.clearTable(DBase.TABLE_ITEMS_NAME);

			Intent intent = new Intent(this, DocsListLoader.class);

			// Копирование параметров интента, в которых содержится в т.ч. и
			// строка подключения.
			intent.putExtras(getIntent());

			// Запуск загрузки строк.
			startActivityForResult(intent, DocsListLoader.DOCSLIST_LOADER_ID);

			// Чтобы обновить список.
			getSupportLoaderManager().getLoader(DOCS_LIST_ID).forceLoad();
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

		switch (v.getId()) {

		case R.id.buttonLoad:

			// Очистка таблиц.
			db.clearTable(DBase.TABLE_DOCS_NAME);
			db.clearTable(DBase.TABLE_ITEMS_NAME);

			startActivityForResult(
					new Intent(this, DocsListLoader.class).putExtra(
							DocsListLoader.CONNECTION_STRING_FIELD_NAME,
							getString(R.string.docsListConnectionString)),
					DocsListLoader.DOCSLIST_LOADER_ID);

			break;

		default:
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);

		MenuItem menuItem;
		menuItem = menu.add(0, CONTEXTMENU_LOAD_BUTTON_ID, 0,
				R.string.btnLoadDocument);
		menuItem.setCheckable(true);
		menuItem.setChecked(true);
		// menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		//
		// @Override
		// public boolean onMenuItemClick(MenuItem item) {
		//
		// setResult(CONTEXTMENU_LOAD_BUTTON_ID);
		//
		// DocsListFragmentActivity.this.finish();
		//
		// return true;
		// }
		// });

		menuItem = menu.add(0, CONTEXTMENU_LOAD_CANCEL_BUTTON_ID, 1,
				R.string.cancel);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == CONTEXTMENU_LOAD_BUTTON_ID) {

			// Загрузка списка документов.
			Cursor cursor = (Cursor) adapter
					.getItem(lastLongClickedItemPosition);

			int numIdx = cursor.getColumnIndex(DBase.FIELD_DOC_NUM_NAME);
			int dateIdx = cursor.getColumnIndex(DBase.FIELD_DOC_DATE_NAME);
			String docNum = cursor.getString(numIdx);
			String docDate = cursor.getString(dateIdx);

			setResult(CONTEXTMENU_LOAD_BUTTON_ID,
					new Intent().putExtra(DBase.FIELD_DOC_NUM_NAME, docNum)
							.putExtra(DBase.FIELD_DOC_DATE_NAME, docDate));

			finish();

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

			Cursor cursor = db.getAllRows(DBase.TABLE_DOCS_NAME);

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

		case DocsListLoader.DOCSLIST_LOADER_ID:

			Message.show("requestCode = " + requestCode + ", resultCode = "
					+ resultCode);

			// Чтобы обновить список.
			getSupportLoaderManager().getLoader(DOCS_LIST_ID).forceLoad();

		}
	}
}
