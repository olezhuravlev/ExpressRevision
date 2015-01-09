package pro.got4.expressrevision;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ItemsListAdapter extends ResourceCursorAdapter implements
		OnClickListener {

	// private Context context;
	private OnItemButtonClickListener listener;

	// ������� �������.
	private int docId_Idx, rowNum_Idx, itemCode_Idx, /* itemDescr_Idx, */
	itemDescrFull_Idx, itemUseSpecif_Idx, specifCode_Idx, specifDescr_Idx,
			measurDescr_Idx, price_Idx, /* quantAcc_Idx, */
			quant_Idx/* , index_Idx */;

	// ������ �����, ������������ ����������.
	private DecimalFormat quantDecimalFormat;

	// private NumberFormat numberFormat;

	// ���������, ������� ������ ������������� ������������ ���������� ��� ����,
	// ����� ��������� ������� ������ �� ��������� ������.
	interface OnItemButtonClickListener {
		public void onItemButtonClick(View v, Cursor cursor);
	}

	public ItemsListAdapter(Context context, Cursor cursor) {

		super(context, R.layout.items_list_item_specif, cursor, true);

		// this.context = context;
		this.listener = (OnItemButtonClickListener) context;

		if (cursor != null)
			fillColumnIndices(cursor);

		// ������ �����, ������������ ����������.
		String quantPattern = mContext
				.getString(R.string.quantityFormatPattern);
		String quantDecimalSeparator = mContext
				.getString(R.string.quantityDecimalSeparator);
		String quantGroupingSeparator = mContext
				.getString(R.string.quantityGroupingSeparator);

		Locale locale = mContext.getResources().getConfiguration().locale;
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		symbols.setDecimalSeparator(quantDecimalSeparator.charAt(0));
		symbols.setGroupingSeparator(quantGroupingSeparator.charAt(0));
		quantDecimalFormat = new DecimalFormat(quantPattern, symbols);
	}

	/*
	 * ������������� �������� �������� �������.
	 */
	private void fillColumnIndices(Cursor cursor) {

		docId_Idx = cursor.getColumnIndex(DBase.FIELD_DOC_ID_NAME);
		rowNum_Idx = cursor.getColumnIndex(DBase.FIELD_ROW_NUM_NAME);
		itemCode_Idx = cursor.getColumnIndex(DBase.FIELD_ITEM_CODE_NAME);
		// itemDescr_Idx = cursor.getColumnIndex(DBase.FIELD_ITEM_DESCR_NAME);
		itemDescrFull_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_DESCR_FULL_NAME);
		itemUseSpecif_Idx = cursor
				.getColumnIndex(DBase.FIELD_ITEM_USE_SPECIF_NAME);
		specifCode_Idx = cursor.getColumnIndex(DBase.FIELD_SPECIF_CODE_NAME);
		specifDescr_Idx = cursor.getColumnIndex(DBase.FIELD_SPECIF_DESCR_NAME);
		measurDescr_Idx = cursor.getColumnIndex(DBase.FIELD_MEASUR_DESCR_NAME);
		price_Idx = cursor.getColumnIndex(DBase.FIELD_PRICE_NAME);
		// quantAcc_Idx = cursor.getColumnIndex(DBase.FIELD_QUANT_ACC_NAME);
		quant_Idx = cursor.getColumnIndex(DBase.FIELD_QUANT_NAME);
		// index_Idx = cursor.getColumnIndex(DBase.FIELD_INDEX_NAME);
	}

	@Override
	public int getItemViewType(int position) {

		Cursor cursor = (Cursor) getItem(position);

		// ���� �������� ������� ���������, ������ �� ������ ��� ��� � �����
		// ����������������.
		if (docId_Idx == rowNum_Idx)
			fillColumnIndices(cursor);

		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);

		switch (itemUseSpecif) {
		case 0: // �������������� �� ������������.
			return 0;
		case 1: // �������������� ������������.
			return 1;
		default:
			return 1;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		// ���� �������� ������� ���������, ������ �� ������ ��� � �����
		// ����������������.
		if (docId_Idx == rowNum_Idx)
			fillColumnIndices(cursor);

		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);
		LayoutInflater li = LayoutInflater.from(context);

		if (itemUseSpecif == 0) {
			// ���� �������������� �� ������������, �� ��������������� ������
			// ��� ���� ��������������.
			return li.inflate(R.layout.items_list_item, parent, false);
		} else {
			// ���� �������������� ������������, �� ��������������� ������ �
			// ����� ��������������.
			return li.inflate(R.layout.items_list_item_specif, parent, false);
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		// ���� �������� ������� ���������, ������ �� ������ ��� � �����
		// ����������������.
		if (docId_Idx == rowNum_Idx)
			fillColumnIndices(cursor);

		// String docId = cursor.getString(docId_Idx);
		int rowNum = cursor.getInt(rowNum_Idx);
		String itemCode = cursor.getString(itemCode_Idx);
		// String itemDescr = cursor.getString(itemDescr_Idx);
		String itemDescrFull = cursor.getString(itemDescrFull_Idx);
		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);
		String specifCode = cursor.getString(specifCode_Idx);
		String specifDescr = cursor.getString(specifDescr_Idx);
		String measurDescr = cursor.getString(measurDescr_Idx);
		float price = cursor.getFloat(price_Idx);
		// float quantAcc = cursor.getFloat(quantAcc_Idx);
		float quant = cursor.getFloat(quant_Idx);
		// String index = cursor.getString(index_Idx);

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
		Button quant_button = (Button) view.findViewById(R.id.quant_button);
		// TextView measur_textView = (TextView) view
		// .findViewById(R.id.measur_textView);
		TextView price_textView = (TextView) view
				.findViewById(R.id.price_textView);
		// TextView currency_textView = (TextView)
		// view.findViewById(R.id.currency_textView);

		row_num_textView.setText(String.valueOf(rowNum));
		item_code_textView.setText(itemCode);
		item_descr_full_textView.setText(itemDescrFull);

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

		// String quantString = numberFormat.format(quant);
		String quantString = quantDecimalFormat.format(quant);
		quant_button.setText(quantString.concat(" ").concat(measurDescr));
		price_textView.setText(String.valueOf(price).concat(" ")
				.concat(context.getString(R.string.currency)));

		// � ���� ������ �������� ����� ������� ��� ����������� �������������
		// ������ ������, � ������� ��������� ������� ������.
		quant_button.setTag(cursor.getPosition());
		quant_button.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.quant_button:

			// � ���� �������� ����� ������� ������ ������� ������.
			// �� ����� ������ ���������� ������������� ������.
			int position = (Integer) v.getTag();
			Cursor cursor = (Cursor) getItem(position);
			listener.onItemButtonClick(v, cursor);
		}
	}
}
