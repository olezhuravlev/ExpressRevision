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

	// Индексы колонок.
	private int docId_Idx, rowNum_Idx, itemCode_Idx, /* itemDescr_Idx, */
	itemDescrFull_Idx, itemUseSpecif_Idx, specifCode_Idx, specifDescr_Idx,
			measurDescr_Idx, price_Idx, /* quantAcc_Idx, */
			quant_Idx/* , index_Idx */, itemVisited_Idx;

	// Формат чисел, отображающих количество.
	private DecimalFormat quantDecimalFormat;

	// private NumberFormat numberFormat;

	// Интерфейс, который должна реализовывать родительская активность для того,
	// чтобы принимать нажатия кнопок на элементах списка.
	interface OnItemButtonClickListener {
		public void onItemButtonClick(View v, Cursor cursor);
	}

	public ItemsListAdapter(Context context, Cursor cursor) {

		super(context, R.layout.items_list_item_specif, cursor, true);

		// this.context = context;
		this.listener = (OnItemButtonClickListener) context;

		if (cursor != null)
			fillColumnIndices(cursor);

		// Формат чисел, отображающих количество.
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
	 * Инициализация значений индексов колонок.
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
		itemVisited_Idx = cursor.getColumnIndex(DBase.FIELD_ITEM_VISITED_NAME);
	}

	@Override
	public int getItemViewType(int position) {

		int itemViewType = 0;

		Cursor cursor = (Cursor) getItem(position);

		// Если соседние индексы одинаковы, значит их вообще еще нет и нужно
		// инициализировать.
		if (docId_Idx == rowNum_Idx)
			fillColumnIndices(cursor);

		int itemVisited = cursor.getInt(itemVisited_Idx);
		int itemUseSpecif = cursor.getInt(itemUseSpecif_Idx);

		if (itemVisited == 0 && itemUseSpecif == 0) {

			// Элемент не посещался, характеристика не используется.
			itemViewType = 0;

		} else if (itemVisited == 0 && itemUseSpecif != 0) {

			// Элемент не посещался, характеристика используется.
			itemViewType = 1;

		} else if (itemVisited != 0 && itemUseSpecif == 0) {

			// Элемент посещался, характеристика не используется.
			itemViewType = 2;

		} else if (itemVisited != 0 && itemUseSpecif != 0) {

			// Элемент посещался, характеристика используется.
			itemViewType = 3;

		} else {
			itemViewType = 1;
		}

		return itemViewType;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		// Если соседние индексы одинаковы, значит их вообще нет и нужно
		// инициализировать.
		if (docId_Idx == rowNum_Idx)
			fillColumnIndices(cursor);

		LayoutInflater li = LayoutInflater.from(context);

		View v = null;

		int itemViewType = getItemViewType(cursor.getPosition());
		switch (itemViewType) {
		case 0: { // Элемент не посещался, характеристика не используется.

			v = li.inflate(R.layout.items_list_item, parent, false);

			break;
		}
		case 1: { // Элемент не посещался, характеристика используется.

			v = li.inflate(R.layout.items_list_item_specif, parent, false);

			break;
		}
		case 2: { // Элемент посещался, характеристика не используется.

			v = li.inflate(R.layout.items_list_item_visited, parent, false);

			break;
		}
		case 3: { // Элемент посещался, характеристика используется.

			v = li.inflate(R.layout.items_list_item_specif_visited, parent,
					false);
			break;
		}

		default:

			// По умолчанию используется непосещенный лайаут с полем
			// характеристики.
			v = li.inflate(R.layout.items_list_item_specif, parent, false);
		}

		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		// Если соседние индексы одинаковы, значит их вообще нет и нужно
		// инициализировать.
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
		int itemVisited = cursor.getInt(itemVisited_Idx);

		TextView row_num_textView = null;
		TextView item_code_textView = null;
		TextView item_descr_full_textView = null;
		TextView specif_code_textView = null;
		TextView specif_descr_textView = null;
		Button quant_button = null;
		TextView quant_textView = null;
		TextView price_textView = null;

		// Непосещенный элемент.
		row_num_textView = (TextView) view.findViewById(R.id.row_num_textView);
		item_code_textView = (TextView) view
				.findViewById(R.id.item_code_textView);
		item_descr_full_textView = (TextView) view
				.findViewById(R.id.item_descr_full_textView);
		specif_code_textView = (TextView) view
				.findViewById(R.id.specif_code_textView);
		specif_descr_textView = (TextView) view
				.findViewById(R.id.specif_descr_textView);
		quant_button = (Button) view.findViewById(R.id.quant_button);
		quant_textView = (TextView) view.findViewById(R.id.quant_textView);
		price_textView = (TextView) view.findViewById(R.id.price_textView);

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

		String quantString = quantDecimalFormat.format(quant);

		if (quant_button != null) {

			quant_button.setText(quantString.concat(" ").concat(measurDescr));

			// В теге кнопки хранится номер позиции для последующей
			// идентификации строки данных, к которым относится нажатая кнопка.
			quant_button.setTag(cursor.getPosition());
			quant_button.setOnClickListener(this);
		}

		if (quant_textView != null)
			quant_textView.setText(quantString.concat(" ").concat(measurDescr));

		price_textView.setText(String.valueOf(price).concat(" ")
				.concat(context.getString(R.string.currency)));
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.quant_button:

			// В теге хранится номер позиции строки нажатой кнопки.
			// По этому номеру происходит идентификация данных.
			int position = (Integer) v.getTag();
			Cursor cursor = (Cursor) getItem(position);
			listener.onItemButtonClick(v, cursor);
		}
	}
}
