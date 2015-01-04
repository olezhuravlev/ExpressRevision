package pro.got4.expressrevision;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class DocsListAdapter extends ResourceCursorAdapter {

	// ‘орматтер даты дл€ преобразовани€ в формат, пон€тный пользователю.
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss", Locale.getDefault());

	// »ндексы колонок.
	private int docNum_Idx, docDate_Idx, docComment_Idx, /* storeCode_Idx, */
	storeDescr_Idx/* , index_Idx */;

	public DocsListAdapter(Context context, Cursor cursor) {

		super(context, R.layout.docs_list_item, cursor, true);

		if (cursor != null)
			fillColumnIndices(cursor);

	}

	/*
	 * »нициализаци€ значений индексов колонок.
	 */
	private void fillColumnIndices(Cursor cursor) {

		docNum_Idx = cursor.getColumnIndex(DBase.FIELD_DOC_NUM_NAME);
		docDate_Idx = cursor.getColumnIndex(DBase.FIELD_DOC_DATE_NAME);
		docComment_Idx = cursor.getColumnIndex(DBase.FIELD_DOC_COMMENT_NAME);
		// storeCode_Idx = cursor.getColumnIndex(DBase.FIELD_STORE_CODE_NAME);
		storeDescr_Idx = cursor.getColumnIndex(DBase.FIELD_STORE_DESCR_NAME);
		// index_Idx = cursor.getColumnIndex(DBase.FIELD_INDEX_NAME);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		// ≈сли соседние индексы одинаковы, значит их вообще нет и нужно
		// инициализировать.
		if (docNum_Idx == docDate_Idx)
			fillColumnIndices(cursor);

		String docNum = cursor.getString(docNum_Idx);
		long docDate = cursor.getLong(docDate_Idx);
		String docComment = cursor.getString(docComment_Idx);
		// String storeCode = cursor.getString(storeCode_Idx);
		String storeDescr = cursor.getString(storeDescr_Idx);
		// String index = cursor.getString(index_Idx);

		TextView numberTextView = (TextView) view
				.findViewById(R.id.numberTextView);
		TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
		TextView commentTextView = (TextView) view
				.findViewById(R.id.commentTextView);
		TextView storeTextView = (TextView) view
				.findViewById(R.id.storeTextView);

		numberTextView.setText(docNum);
		dateTextView.setText(dateFormatter.format(docDate));
		commentTextView.setText(docComment);
		storeTextView.setText(storeDescr);
	}
}
