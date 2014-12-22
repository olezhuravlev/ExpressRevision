package pro.got4.expressrevision;

import android.content.Context;
import android.content.Intent;
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

	public static final int DOCS_LIST_ID = 1;

	public static final int CONTEXTMENU_LOAD_BUTTON_ID = 1;
	public static final int CONTEXTMENU_LOAD_CANCEL_BUTTON_ID = 2;

	private ListView lvData;

	private SimpleCursorAdapter scAdapter;

	private Button buttonLoad;

	public DBase db;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.docs_list);

		// ��������� ����������� � ��
		db = new DBase(this);
		db.open();

		buttonLoad = (Button) findViewById(R.id.buttonLoad);
		buttonLoad.setOnClickListener(this);

		// ��������� ������� �������������
		String[] from = new String[] { DBase.FIELD_REF_NAME,
				DBase.FIELD_NUM_NAME, DBase.FIELD_DATE_NAME,
				DBase.FIELD_STORE_NAME, DBase.FIELD_COMMENT_NAME };
		int[] to = new int[] { R.id.refTextView, R.id.numberTextView,
				R.id.dateTextView, R.id.storeTextView, R.id.commentTextView };

		// ������� ������� � ����������� ������
		scAdapter = new SimpleCursorAdapter(this, R.layout.docs_list_item,
				null, from, to,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		lvData = (ListView) findViewById(R.id.docsListView);
		lvData.setAdapter(scAdapter);

		lvData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});

		// ��������� ����������� ���� � ������.
		registerForContextMenu(lvData);

		// ��������� ��� ������ ������.
		getSupportLoaderManager().initLoader(DOCS_LIST_ID, null, this);

		// ���� ���������� ������������� ���������� ������ ����������, ��
		// �������� ��� � ������� ��������.
		// ����� ������� ���������, �� �������� �� ������ ������ ����������
		// ������������ ���������� ��������� ������������ (savedInstanceState ==
		// null). � ��������� ������,���� ��������� ��������� � ������� � �
		// �������� ��� �������� ������������, �� �������� ����� ����������
		// ����� ���������� �������� �����!
		if (Main.docsListNeedsToBeFetched() && savedInstanceState == null) {

			// ������� ������.
			db.clearTable(DBase.TABLE_DOCS_NAME);
			db.clearTable(DBase.TABLE_ITEMS_NAME);

			startActivityForResult(new Intent(this, DocsListLoader.class),
					DocsListLoader.DOCSLIST_LOADER_ID);

			// ����� �������� ������.
			getSupportLoaderManager().getLoader(DOCS_LIST_ID).forceLoad();
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		// �����, ��������� �� ������.
		Main.setStyle(this);
	}

	/**
	 * ���������� ������� ������.
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.buttonLoad:

			// ������� ������.
			db.clearTable(DBase.TABLE_DOCS_NAME);
			db.clearTable(DBase.TABLE_ITEMS_NAME);

			startActivityForResult(new Intent(this, DocsListLoader.class),
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

			// �������� ������ ����������.
			setResult(CONTEXTMENU_LOAD_BUTTON_ID);

			finish();

			return true;

		} else if (item.getItemId() == CONTEXTMENU_LOAD_CANCEL_BUTTON_ID) {

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

		scAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		scAdapter.swapCursor(null);
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

			// ����� �������� ������.
			getSupportLoaderManager().getLoader(DOCS_LIST_ID).forceLoad();

		}
	}
}
