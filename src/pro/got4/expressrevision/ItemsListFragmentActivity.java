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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ItemsListFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnClickListener {

	public static final int ITEMS_LIST_ID = 2;

	public static final int CONTEXTMENU_LOAD_BUTTON_ID = 1;
	public static final int CONTEXTMENU_LOAD_CANCEL_BUTTON_ID = 2;

	public static final String START_ITEMS_LOADER = "start_items_loader";

	private ListView lvData;

	private SimpleCursorAdapter scAdapter;

	public DBase db;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.items_list);

		// ��������� ����������� � ��
		db = new DBase(this);
		db.open();

		// ��������� ������� �������������
		String[] from = new String[] { DBase.FIELD_CODE_NAME,
				DBase.FIELD_NAME_NAME, DBase.FIELD_NAMEFULL_NAME };
		int[] to = new int[] { R.id.textViewID, R.id.textViewName,
				R.id.textViewNameFull };

		// ������� ������� � ����������� ������
		scAdapter = new SimpleCursorAdapter(this, R.layout.items_list_item,
				null, from, to,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		lvData = (ListView) findViewById(R.id.listViewItems);
		lvData.setAdapter(scAdapter);

		lvData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});

		// ��������� ����������� ���� � ������.
		//registerForContextMenu(lvData);

		// ��������� ��� ������ ������.
		getSupportLoaderManager().initLoader(ITEMS_LIST_ID, null, this);

		// ���� ������ ���� ������ ��������, �� ��� ����������.
		Message.show("[hashCode = " + this.hashCode()
				+ "], getIntent().hashCode() = [" + getIntent().hashCode()
				+ "]");
		if (getIntent().getExtras().getBoolean(START_ITEMS_LOADER) == true
				&& savedInstanceState == null) {

			// ������� ������� ����������� ���������.
			db.clearTable(DBase.TABLE_ITEMS_NAME);

			startActivityForResult(new Intent(this, ItemsListLoader.class),
					ItemsListLoader.ITEMSLIST_LOADER_ID);

			// ����� �������� ������.
			getSupportLoaderManager().getLoader(ITEMS_LIST_ID).forceLoad();
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

				// ���� ��������� �� ��� ������, �� �� ����������� ������
				// ���������, �.�. �������� �.�. �������� ������ ���������, ���
				// �� ���� �������� �����.
				// ��������! ��������� ���������� � ����� ���������� �� ������,
				// ������ ��� �� ����������� ������� � ���� ������ �����������
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

		// ����� �������� ������.
		getSupportLoaderManager().getLoader(ITEMS_LIST_ID).forceLoad();
	}
}
