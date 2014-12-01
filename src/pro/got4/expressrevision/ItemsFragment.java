//package pro.got4.expressrevision;
//
//import android.database.Cursor;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ListView;
//
//public class ItemsFragment extends Fragment {
//
//	DBase db;
//	ListView lv;
//	Cursor cursor;
//	ListViewItemsAdapter adapter;
//
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//
//		View v = inflater.inflate(R.layout.items_list, null);
//
//		db = new DBase(getActivity());
//		db.open();
//
//		lv = (ListView) v.findViewById(R.id.listViewItems);
//
//		cursor = db.getSQLiteDatabase().rawQuery("SELECT * FROM items", null);
//		adapter = new ListViewItemsAdapter(getActivity(), this.cursor);
//		lv.setAdapter(adapter);
//
//		EditText editTextFilter = (EditText) v
//				.findViewById(R.id.editTextFilter);
//		editTextFilter.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before,
//					int count) {
//
//				cursor = db.getSQLiteDatabase().rawQuery(
//						"SELECT * FROM " + DBase.TABLE_ITEMS_NAME + " WHERE "
//								+ DBase.FIELD_INDEX_NAME + " GLOB '*"
//								+ s.toString().toLowerCase() + "*'", null);
//				adapter.changeCursor(cursor);
//				System.out.println("Filter = " + s);
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//			}
//		});
//
//		return v;
//	}
//}
