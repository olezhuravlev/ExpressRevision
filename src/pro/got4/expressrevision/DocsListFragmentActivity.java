package pro.got4.expressrevision;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

	public static final int ID = 100;

	public static final int CONTEXTMENU_LOAD_BUTTON_ID = 1;
	public static final int CONTEXTMENU_LOAD_CANCEL_BUTTON_ID = 2;

	private ListView lvData;

	private DocsListAdapter adapter;

	private Button buttonLoad;

	public DBase db;

	private int lastLongClickedItemPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.docs_list);

		// ��������� ����������� � ��
		db = new DBase(this);
		db.open();

		buttonLoad = (Button) findViewById(R.id.buttonLoad);
		buttonLoad.setOnClickListener(this);

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

		// ��������� ����������� ���� � ������.
		registerForContextMenu(lvData);

		// ��������� ��� ������ ������.
		getSupportLoaderManager().initLoader(ID, null, this);

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

			Intent intent = new Intent(this, DocsListLoader.class);

			// ����������� ���������� �������, � ������� ���������� � �.�. �
			// ������ �����������.
			intent.putExtras(getIntent());

			// ������ �������� �����.
			startActivityForResult(intent, DocsListLoader.ID);

			// ����� �������� ������.
			getSupportLoaderManager().getLoader(ID).forceLoad();
		}
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

			String connectionString = PreferenceManager
					.getDefaultSharedPreferences(this).getString(
							DocsListLoader.CONNECTION_STRING_FIELD_NAME, "");

			startActivityForResult(
					new Intent(this, DocsListLoader.class).putExtra(
							DocsListLoader.CONNECTION_STRING_FIELD_NAME,
							connectionString), DocsListLoader.ID);

			break;

		default:
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		MenuItem menuItem;
		menuItem = menu.add(0, CONTEXTMENU_LOAD_BUTTON_ID, 0,
				R.string.loadDocument);
		menuItem.setCheckable(true);
		menuItem.setChecked(true);

		menuItem = menu.add(0, CONTEXTMENU_LOAD_CANCEL_BUTTON_ID, 1,
				R.string.cancel);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == CONTEXTMENU_LOAD_BUTTON_ID) {

			// ��������� ����������� ��� �� ����������� �������� � ����������,
			// ���������� �� �������� ������ ����������.
			Cursor cursor = (Cursor) adapter
					.getItem(lastLongClickedItemPosition);

			int numIdx = cursor.getColumnIndex(DBase.FIELD_DOC_NUM_NAME);
			int dateIdx = cursor.getColumnIndex(DBase.FIELD_DOC_DATE_NAME);
			int rowsIdx = cursor.getColumnIndex(DBase.FIELD_DOC_ROWS_NAME);

			String docNum = cursor.getString(numIdx);
			// String docDate = cursor.getString(dateIdx);
			long docDate = cursor.getLong(dateIdx);
			long rows = cursor.getLong(rowsIdx);

			setResult(CONTEXTMENU_LOAD_BUTTON_ID,
					new Intent().putExtra(DBase.FIELD_DOC_NUM_NAME, docNum)
							.putExtra(DBase.FIELD_DOC_DATE_NAME, docDate)
							.putExtra(DBase.FIELD_DOC_ROWS_NAME, rows));

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

			Cursor cursor = db.getRowsAll(DBase.TABLE_DOCS_NAME, null);

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

		case DocsListLoader.ID:

			// ����� �������� ������.
			getSupportLoaderManager().getLoader(ID).forceLoad();
		}
	}
}
