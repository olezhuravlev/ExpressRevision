package pro.got4.expressrevision;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class DocsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor> {

	private static final int CM_DELETE_ID = 1;
	ListView lvData;
	DBase db;
	SimpleCursorAdapter scAdapter;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.docs_list);

		// открываем подключение к БД
		db = new DBase(this);
		db.open();

		// формируем столбцы сопоставления
		String[] from = new String[] { DBase.FIELD_REF_NAME,
				DBase.FIELD_NUM_NAME, DBase.FIELD_DATE_NAME,
				DBase.FIELD_STORE_NAME, DBase.FIELD_COMMENT_NAME };
		int[] to = new int[] { R.id.refTextView, R.id.numberTextView,
				R.id.dateTextView, R.id.storeTextView, R.id.commentTextView };

		// создаем адаптер и настраиваем список
		scAdapter = new SimpleCursorAdapter(this, R.layout.docs_list_item,
				null, from, to,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		lvData = (ListView) findViewById(R.id.docsListView);
		lvData.setAdapter(scAdapter);

		lvData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				System.out.println("onItemClick");
			}
		});

		// добавляем контекстное меню к списку
		registerForContextMenu(lvData);

		// создаем лоадер для чтения данных
		System.out.println("before initLoader");

		getSupportLoaderManager().initLoader(0, null, this);

		System.out.println("after initLoader");
	}

	// обработка нажатия кнопки
	public void onButtonClick(View view) {
		// // добавляем запись
		// db.addRec("sometext " + (scAdapter.getCount() + 1),
		// R.drawable.ic_launcher);
		// получаем новый курсор с данными
		getSupportLoaderManager().getLoader(0).forceLoad();
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CM_DELETE_ID, 0, R.string.btnLoadDocument);
	}

	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == CM_DELETE_ID) {

			// получаем из пункта контекстного меню данные по пункту списка
			// AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
			// .getMenuInfo();

			// извлекаем id записи и удаляем соответствующую запись в БД
			// db.delRec(acmi.id);

			// получаем новый курсор с данными
			getSupportLoaderManager().getLoader(0).forceLoad();

			return true;
		}
		return super.onContextItemSelected(item);
	}

	protected void onDestroy() {

		super.onDestroy();

		// закрываем подключение при выходе
		db.close();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {

		System.out.println("onCreateLoader");

		return new MyCursorLoader(this, db);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		System.out.println("onLoadFinished");

		scAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		System.out.println("onLoaderReset");
	}

	static class MyCursorLoader extends CursorLoader {

		DBase db;

		public MyCursorLoader(Context context, DBase db) {
			super(context);
			this.db = db;
		}

		@Override
		public Cursor loadInBackground() {

			System.out.println("loadInBackground");

			Cursor cursor = db.getAllDocs();

			System.out.println("cursor has [" + cursor.getCount() + "] rows");

			try {

				for (int i = 0; i < 10; i++) {
					TimeUnit.MILLISECONDS.sleep(50); // TODO ЗАДЕРЖКА!!!
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return cursor;
		}

		@Override
		public void deliverResult(Cursor cursor) {

			System.out.println("deliverResult");

			super.deliverResult(cursor);

			if (isStarted()) {

			}
		}

	}
}
