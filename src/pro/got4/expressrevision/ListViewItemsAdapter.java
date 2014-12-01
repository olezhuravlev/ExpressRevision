//package pro.got4.expressrevision;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ResourceCursorAdapter;
//import android.widget.TextView;
//
//public class ListViewItemsAdapter extends ResourceCursorAdapter {
//
//	public ListViewItemsAdapter(Context context, Cursor cursor) {
//		super(context, R.layout.items_list_item, cursor, true);
//	}
//
//	@Override
//	public View newView(Context context, Cursor cursor, ViewGroup parent) {
//
//		LayoutInflater li = LayoutInflater.from(context);
//
//		return li.inflate(R.layout.items_list_item, parent, false);
//	}
//
//	@Override
//	public void bindView(View view, Context context, Cursor cursor) {
//
//		TextView textViewID = (TextView) view.findViewById(R.id.textViewID);
//		TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
//		TextView textViewNameFull = (TextView) view
//				.findViewById(R.id.textViewNameFull);
//
//		// определяем номера столбцов по имени в выборке
//		int idCodeIndex = cursor.getColumnIndex(DBase.FIELD_CODE_NAME);
//		int nameColIndex = cursor.getColumnIndex(DBase.FIELD_NAME_NAME);
//		int nameFullColIndex = cursor.getColumnIndex(DBase.FIELD_NAMEFULL_NAME);
//
//		String id = cursor.getString(idCodeIndex);
//		String name = cursor.getString(nameColIndex);
//		String nameFull = cursor.getString(nameFullColIndex);
//
//		textViewID.setText(id.toString());
//		textViewName.setText(name);
//		textViewNameFull.setText(nameFull);
//	}
//}
