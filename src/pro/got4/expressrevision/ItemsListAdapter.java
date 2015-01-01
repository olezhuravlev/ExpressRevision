package pro.got4.expressrevision;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ItemsListAdapter extends ResourceCursorAdapter {

	View layout;
	View layoutSpecif;
	Context context;

	public ItemsListAdapter(Context context, Cursor cursor) {
		super(context, R.layout.items_list_item_specif, cursor, true);
		this.context = context;
	}

	@Override
	public int getItemViewType(int position) {
		if (position < 3)
			return 1; // С характеристикой.
		else
			return 0; // Без характеристики.
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	//
	// Message.show();
	// // int viewType = this.getItemViewType(position);
	// // switch (viewType) {
	// // case 0:
	// //
	// // LayoutInflater li = (LayoutInflater) context
	// // .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// // layout = li.inflate(R.layout.items_list_item, parent, false);
	// //
	// // break;
	// //
	// // case 1:
	// //
	// // break;
	// // }
	// return super.getView(position, convertView, parent);
	// }

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		Message.show();

		int itemUseSpecif_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_USE_SPECIF_NAME);
		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);
		LayoutInflater li = LayoutInflater.from(context);

		// Message.show("newView, itemUseSpecif = " + itemUseSpecif);

		if (itemUseSpecif == 0) {
			// Если характеристика не используется, то разворачивается лайаут
			// без поля характеристики.
			layout = li.inflate(R.layout.items_list_item, parent, false);
			return layout;
		} else {
			// Если характеристика используется, то разворачивается лайаут с
			// полем характеристики.
			layoutSpecif = li.inflate(R.layout.items_list_item_specif, parent,
					false);
			return layoutSpecif;
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		Message.show();

		int docId_Idx = cursor.getColumnIndex(DBase.FIELD_DOC_ID_NAME);
		int rowNum_Idx = cursor.getColumnIndex(DBase.FIELD_ROW_NUM_NAME);
		int itemCode_Idx = cursor.getColumnIndex(DBase.FIELD_ITEM_CODE_NAME);
		int itemDescr_Idx = cursor.getColumnIndex(DBase.FIELD_ITEM_DESCR_NAME);
		int itemDescrFull_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_DESCR_FULL_NAME);
		int itemUseSpecif_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_USE_SPECIF_NAME);
		int specifCode_Idx = cursor
				.getColumnIndex(DBase.FIELD_SPECIF_CODE_NAME);
		int specifDescr_Idx = cursor
				.getColumnIndex(DBase.FIELD_SPECIF_DESCR_NAME);
		int measurDescr_Idx = cursor
				.getColumnIndex(DBase.FIELD_MEASUR_DESCR_NAME);
		int price_Idx = cursor.getColumnIndex(DBase.FIELD_PRICE_NAME);
		int quantAcc_Idx = cursor.getColumnIndex(DBase.FIELD_QUANT_ACC_NAME);
		int quant_Idx = cursor.getColumnIndex(DBase.FIELD_QUANT_NAME);
		int index_Idx = cursor.getColumnIndex(DBase.FIELD_INDEX_NAME);

		String docId = cursor.getString(docId_Idx);
		int rowNum = cursor.getInt(rowNum_Idx);
		String itemCode = cursor.getString(itemCode_Idx);
		String itemDescr = cursor.getString(itemDescr_Idx);
		String itemDescrFull = cursor.getString(itemDescrFull_Idx);
		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);
		String specifCode = cursor.getString(specifCode_Idx);
		String specifDescr = cursor.getString(specifDescr_Idx);
		String measurDescr = cursor.getString(measurDescr_Idx);
		float price = cursor.getFloat(price_Idx);
		float quantAcc = cursor.getFloat(quantAcc_Idx);
		float quant = cursor.getFloat(quant_Idx);
		String index = cursor.getString(index_Idx);

		TextView row_num_textView = (TextView) view
				.findViewById(R.id.row_num_textView);
		TextView item_code_textView = (TextView) view
				.findViewById(R.id.item_code_textView);
		// TextView item_descr_textView = (TextView)
		// view.findViewById(R.id.item_descr_textView);
		TextView item_descr_full_textView = (TextView) view
				.findViewById(R.id.item_descr_full_textView);
		TextView specif_code_textView = (TextView) view
				.findViewById(R.id.specif_code_textView);
		TextView specif_descr_textView = (TextView) view
				.findViewById(R.id.specif_descr_textView);
		TextView quant_button = (TextView) view.findViewById(R.id.quant_button);
		// TextView measur_textView = (TextView) view
		// .findViewById(R.id.measur_textView);
		TextView price_textView = (TextView) view
				.findViewById(R.id.price_textView);
		// TextView currency_textView = (TextView)
		// view.findViewById(R.id.currency_textView);

		row_num_textView.setText(String.valueOf(rowNum));
		item_code_textView.setText(itemCode);
		item_descr_full_textView.setText(itemDescrFull);

		Message.show("bindView, itemUseSpecif = " + itemUseSpecif);
		// Message.show("bindView, specif_code_textView = " +
		// specif_code_textView);
		// Message.show("bindView, specif_descr_textView = "
		// + specif_descr_textView);
		if (itemUseSpecif != 0) {

			if (specif_code_textView != null) {
				specif_code_textView.setText(specifCode);
			}

			if (specif_descr_textView != null) {
				specif_descr_textView.setText(specifDescr);
			}
		} else {

			if (specif_code_textView != null) {
				specif_code_textView.setText("");
			}

			if (specif_descr_textView != null) {
				specif_descr_textView.setText("");
			}
		}

		quant_button.setText(String.valueOf(quant).concat(" ")
				.concat(measurDescr));
		// measur_textView.setText(measurDescr);
		price_textView.setText(String.valueOf(price).concat(" ")
				.concat(context.getString(R.string.currency)));
	}
}
