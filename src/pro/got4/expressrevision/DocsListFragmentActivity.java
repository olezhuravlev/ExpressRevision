package pro.got4.expressrevision;

import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class DocsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnClickListener {

	private static final int CONTEXTMENU_LOAD_BUTTON_ID = 0;
	private static final int LOADER_DOCUMENTS_ID = 0;

	ListView lvData;
	DBase db;
	SimpleCursorAdapter scAdapter;

	Button buttonLoad;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		System.out.println("onCreate()");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.docs_list);

		buttonLoad = (Button) findViewById(R.id.buttonLoad);
		buttonLoad.setOnClickListener(this);

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

		// initLoader loads the data that was loaded in the last run, if it had
		// run before. This is why you call in in the initialization method of
		// your Fragment/Activity. This is handy because you won't have to
		// requery on orientation change.

		// Ensures a loader is initialized and active. If the loader doesn't
		// already exist, one is created and (if the activity/fragment is
		// currently started) starts the loader. Otherwise the last created
		// loader is re-used.

		// In either case, the given callback is associated with the loader, and
		// will be called as the loader state changes. If at the point of call
		// the caller is in its started state, and the requested loader already
		// exists and has generated its data, then callback
		// onLoadFinished(Loader, D) will be called immediately (inside of this
		// function), so you must be prepared for this to happen.
		getSupportLoaderManager().initLoader(LOADER_DOCUMENTS_ID, null, this);

		// restartLoader cleans up previously loaded data so that you get a new
		// Loader to work with (likely) different data.
		// getSupportLoaderManager().restartLoader(0, null, this);

		System.out.println("after initLoader");
	}

	/**
	 * Обработчик нажатия кнопок.
	 */
	@Override
	public void onClick(View v) {

		// db.open();

		ContentValues docsValues = new ContentValues();

		docsValues.put(DBase.FIELD_REF_NAME, "new");
		docsValues.put(DBase.FIELD_NUM_NAME, "ЭКС00054__");
		docsValues.put(DBase.FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(DBase.FIELD_STORE_NAME, "___ киоск");
		docsValues.put(DBase.FIELD_COMMENT_NAME, "");

		db.insert(DBase.TABLE_DOCS_NAME, docsValues);

		// db.close();

		// Перезапускает загрузку, не пересоздавая загрузчик.
		// Эта функция используется, например, в дефолтной реализации
		// onContentChanged() при обнаружении изменений в источнике.
		getSupportLoaderManager().getLoader(LOADER_DOCUMENTS_ID).forceLoad();

		// Пересоздает загрузчик, уничтожая старый.
		// Внутри все равно вызывается forceLoad().
		// getSupportLoaderManager()
		// .restartLoader(LOADER_DOCUMENTS_ID, null, this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXTMENU_LOAD_BUTTON_ID, 0, R.string.btnLoadDocument);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		// Загрузка списка документов.
		if (item.getItemId() == CONTEXTMENU_LOAD_BUTTON_ID) {

			// получаем новый курсор с данными
			getSupportLoaderManager().getLoader(0).forceLoad();

			return true;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onDestroy() {

		System.out.println("onDestroy()");

		super.onDestroy();

		// Т.к. при изменении конфигурации менеджером используется то же самое
		// подключение, то если здесь его закрыть,
		// то добавить записи уже не удасться!
		// db.close();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {

		// Метод вызывается при вызове метода initLoader().
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

		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		scAdapter.swapCursor(null);
	}

	private static class MyCursorLoader extends CursorLoader {

		private DBase db;

		private MyCursorLoader(Context context, DBase db) {

			super(context);

			System.out.println("*** MyCursorLoader ***");
			this.db = db;
		}

		@Override
		public Cursor loadInBackground() {

			System.out.println("loadInBackground");

			Cursor cursor = db.getAllRows();

			System.out.println("cursor has [" + cursor.getCount() + "] rows");

			try {

				for (int i = 0; i < 5; i++) {
					System.out.println("loadInBackground: sleep()");
					TimeUnit.MILLISECONDS.sleep(1000); // TODO ЗАДЕРЖКА!!!
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("loadInBackground: return cursor;");

			return cursor;
		}

		@Override
		public void deliverResult(Cursor cursor) {

			System.out.println("deliverResult");
			super.deliverResult(cursor);

		}

	}
}
