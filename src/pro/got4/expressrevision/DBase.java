package pro.got4.expressrevision;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBase {

	public static final String DB_NAME = "ExpressRevision";

	// ������� ����������.
	public static final String TABLE_DOCS_DEMO_NAME = "documents_demo";
	public static final String TABLE_DOCS_NAME = "documents";

	// ���� ������ ����������.
	public static final String FIELD_DOC_NUM_NAME = "doc_num";
	public static final String FIELD_DOC_DATE_NAME = "doc_date";
	public static final String FIELD_DOC_COMMENT_NAME = "doc_comm";
	public static final String FIELD_DOC_ROWS_NAME = "doc_rows";
	public static final String FIELD_STORE_CODE_NAME = "store_code";
	public static final String FIELD_STORE_DESCR_NAME = "store_descr";

	// ������� ������������ ����������� ����������.
	public static final String TABLE_ITEMS_DEMO_NAME = "items_demo";
	public static final String TABLE_ITEMS_NAME = "items";

	// ���� ������ ������������ ����������� ����������.
	public static final String FIELD_ROW_NUM_NAME = "row_num";
	public static final String FIELD_ITEM_CODE_NAME = "item_code";
	public static final String FIELD_ITEM_DESCR_NAME = "item_descr";
	public static final String FIELD_ITEM_DESCR_FULL_NAME = "item_descr_full";
	public static final String FIELD_ITEM_USE_SPECIF_NAME = "item_use_specif";
	public static final String FIELD_SPECIF_CODE_NAME = "specif_code";
	public static final String FIELD_SPECIF_DESCR_NAME = "specif_descr";
	public static final String FIELD_MEASUR_DESCR_NAME = "measur_descr";
	public static final String FIELD_PRICE_NAME = "price";
	public static final String FIELD_QUANT_ACC_NAME = "quant_acc";
	public static final String FIELD_QUANT_NAME = "quant";
	public static final String FIELD_ITEM_VISITED_NAME = "visited";

	// ���� �������������� ����������, �������������� � � ������� ����������, �
	// � ������� ������������.
	public static final String FIELD_DOC_ID_NAME = "doc_id";

	// ��������� ����, ������������ ���������� ��� ������������� ������.
	// �������� ��� ��� ������!!!
	public static final String FIELD_ID_NAME = "_id";

	// ��������� ���� ��� �������������� � ������, �������� ������������.
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss", Locale.getDefault());

	// ��������� ���� ��� ���������� ���� � �������, ������������� �
	// �������������� ���������.
	public static final SimpleDateFormat dateIDFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	// ��������� ����, ������������ ��� ������ � ��������.
	public static final String FIELD_INDEX_NAME = "idx";

	private final static int DBVersion = 1;

	private Context context;
	private DBaseHelper dbHelper;
	private SQLiteDatabase sqliteDb;

	public DBase(Context context) {
		this.context = context;
	}

	public void open() {
		dbHelper = new DBaseHelper();
		sqliteDb = dbHelper.getWritableDatabase();
	}

	public void close() {
		sqliteDb.close();
	}

	/**
	 * ���������� ������ � ��. � ����������� �� ������� ������������ ������
	 * ����� �����, ����������� ���� ������.
	 * 
	 * @param dataBase
	 * @param tableName
	 * @param values
	 * @return
	 */
	// ��� ������ ������������ ��� �������������� ������������� ��.
	public long insert(SQLiteDatabase dataBase, String tableName,
			ContentValues values) {

		// ���������� ����, ������������� ��� ������.
		addItemIndexField(tableName, values);

		return dataBase.insert(tableName, null, values);
	}

	/**
	 * ��������� ���� ������ � ��������� ���������������.
	 * 
	 * @param dataBase
	 * @param tableName
	 * @param row_id
	 * @param values
	 * @return
	 */
	public int update(SQLiteDatabase dataBase, String table,
			ContentValues values, String whereClause, String[] whereArgs) {

		int rowsAffected = dataBase.update(table, values, whereClause,
				whereArgs);

		return rowsAffected;
	}

	/**
	 * ��������� ���� ������ � ��������� ���������������.
	 * 
	 * @param dataBase
	 * @param tableName
	 * @param row_id
	 * @param values
	 * @return
	 */
	public int update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {

		int rowsAffected = sqliteDb.update(table, values, whereClause,
				whereArgs);

		return rowsAffected;
	}

	public SQLiteDatabase getSQLiteDatabase() {
		return sqliteDb;
	}

	private class DBaseHelper extends SQLiteOpenHelper {

		public DBaseHelper() {
			super(DBase.this.context, DB_NAME, null, DBVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			// �������� ������ ������ ����������.
			// �������, �������� ����� ���������� ��� ����-������.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_DEMO_NAME + " ("
					+ FIELD_ID_NAME + " integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " integer, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_DOC_ROWS_NAME + " integer, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_DOC_ID_NAME
					+ " text," + FIELD_INDEX_NAME + " text);");

			// �������, �������� �������� ����� ����������.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_NAME + " (" + FIELD_ID_NAME
					+ " integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " integer, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_DOC_ROWS_NAME + " integer, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_DOC_ID_NAME
					+ " text," + FIELD_INDEX_NAME + " text);");

			// �������� ������ ������������ ����������� ����������.
			// �������, �������� ����� ������������ ��� ����������������
			// ����������.
			db.execSQL("CREATE TABLE " + TABLE_ITEMS_DEMO_NAME + " ("
					+ FIELD_ID_NAME + " integer primary key autoincrement, "
					+ FIELD_DOC_ID_NAME + " text," + FIELD_ROW_NUM_NAME
					+ " integer, " + FIELD_ITEM_CODE_NAME + " text, "
					+ FIELD_ITEM_DESCR_NAME + " text, "
					+ FIELD_ITEM_DESCR_FULL_NAME + " text, "
					+ FIELD_ITEM_USE_SPECIF_NAME + " integer, "
					+ FIELD_SPECIF_CODE_NAME + " text, "
					+ FIELD_SPECIF_DESCR_NAME + " text, "
					+ FIELD_MEASUR_DESCR_NAME + " text, " + FIELD_PRICE_NAME
					+ " real, " + FIELD_QUANT_ACC_NAME + " real, "
					+ FIELD_QUANT_NAME + " real, " + FIELD_ITEM_VISITED_NAME
					+ " integer, " + FIELD_INDEX_NAME + " text);");

			// �������, �������� ����� ������������ ��� �������� ����������.
			db.execSQL("CREATE TABLE " + TABLE_ITEMS_NAME + " ("
					+ FIELD_ID_NAME + " integer primary key autoincrement, "
					+ FIELD_DOC_ID_NAME + " text," + FIELD_ROW_NUM_NAME
					+ " integer, " + FIELD_ITEM_CODE_NAME + " text, "
					+ FIELD_ITEM_DESCR_NAME + " text, "
					+ FIELD_ITEM_DESCR_FULL_NAME + " text, "
					+ FIELD_ITEM_USE_SPECIF_NAME + " integer, "
					+ FIELD_SPECIF_CODE_NAME + " text, "
					+ FIELD_SPECIF_DESCR_NAME + " text, "
					+ FIELD_MEASUR_DESCR_NAME + " text, " + FIELD_PRICE_NAME
					+ " real, " + FIELD_QUANT_ACC_NAME + " real, "
					+ FIELD_QUANT_NAME + " real, " + FIELD_ITEM_VISITED_NAME
					+ " integer, " + FIELD_INDEX_NAME + " text);");

			insertDemoItems(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// ����� �������� ��� ���������� ��������� ��.
		}
	}

	/**
	 * ��������� ��������� ContentValues ��������� �����.<br>
	 * ��������� ���� ����������� ������ � ��� ������, ���� ��� � ��������� ���
	 * ���.
	 * 
	 * @param values
	 * @return String, ������� ���� ��������� ��������� ����.
	 */
	public String addItemIndexField(String tableName, ContentValues values) {

		String indexValue = (String) values.get(FIELD_INDEX_NAME);

		if (indexValue != null)
			return indexValue;

		String[] indexFields = null;
		if (tableName.equals(TABLE_DOCS_NAME)) {

			indexFields = new String[] { FIELD_DOC_NUM_NAME,
					FIELD_DOC_DATE_NAME, FIELD_DOC_COMMENT_NAME,
					FIELD_STORE_DESCR_NAME };

		} else if (tableName.equals(TABLE_ITEMS_NAME)) {

			indexFields = new String[] { FIELD_ITEM_CODE_NAME,
					FIELD_ITEM_DESCR_NAME, FIELD_ITEM_DESCR_FULL_NAME,
					FIELD_SPECIF_CODE_NAME, FIELD_SPECIF_DESCR_NAME };

		}

		indexValue = getContentValuesString(values, indexFields, true)
				.toLowerCase(Locale.getDefault());

		values.put(FIELD_INDEX_NAME, indexValue);

		return indexValue;
	}

	/**
	 * ���������� ���������� ��������� ContentValues � ���� ������.<br>
	 * ������������ ��� ���������� ���� ������.
	 * 
	 * @param values
	 * @param keys
	 * @param uniqueOnly
	 *            - ���� ����, ��� � ������ ����� ��������� ������ ����������
	 *            ��������.
	 * @return
	 */
	public String getContentValuesString(ContentValues values, String[] keys,
			boolean uniqueOnly) {

		String result = "";

		if (keys == null)
			return result;

		for (int i = 0; i < keys.length; i++) {

			String fieldName = keys[i];
			String currentValue = "";

			// �������������� ���� ��������� � �������� ������.
			if (fieldName.equals(FIELD_DOC_DATE_NAME)) {
				long gmt = values.getAsLong(fieldName);
				currentValue = dateFormatter.format(gmt);
			} else {
				currentValue = values.getAsString(fieldName);
			}

			if (currentValue != null) {
				if (uniqueOnly) {
					if (result.indexOf(currentValue) == -1) {
						result += currentValue;
					}
				} else {
					result += currentValue;
				}
			}
		}

		return result;
	}

	/**
	 * ���������� ������ �� ������ ������� ������������ � ���������
	 * ���������������. ������ ����� ���������� �� ������� ��� ������ ������.
	 * 
	 * @return
	 */
	public Cursor getItemRowById(int rowNum) {

		String[] selectionArgs = { String.valueOf(rowNum) };
		Cursor cursor = sqliteDb.query(DBase.TABLE_ITEMS_NAME, null,
				DBase.FIELD_ID_NAME + " = ?", selectionArgs, null, null, null);

		cursor.moveToFirst();
		if (cursor.isFirst()) {
			return cursor;

		} else {
			return null;
		}
	}

	/**
	 * ���������� ������ �� �� ���������� �������.
	 * 
	 * @return
	 */
	public Cursor getRowsAll(String tableName, String orderBy) {
		return sqliteDb.query(tableName, null, null, null, null, null, orderBy);
	}

	/**
	 * ���������� ������ �� ���������� �������, ���������� �� �������.
	 * 
	 * @return
	 */
	public Cursor getRowsFiltered(String tableName, String selection,
			String selectionArg, String orderBy) {

		String[] selectionArgs = { selectionArg };

		return sqliteDb.query(tableName, null, selection, selectionArgs, null,
				null, orderBy);
	}

	/**
	 * ���������� ������ �� ���������� �������, ���������� �� ��������� LIKE.
	 */
	public Cursor getRowsFilteredLike(String tableName, String filterString) {

		String filter = FIELD_INDEX_NAME.concat(" LIKE '%")
				.concat(filterString).concat("%'");

		return sqliteDb.query(tableName, null, filter, null, null, null, null);
	}

	/**
	 * ��������� ������������ ������ � ������� ��.
	 */
	public Cursor getRows(String sql, String[] selectionArgs) {

		return sqliteDb.rawQuery(sql, selectionArgs);
	}

	/**
	 * ���������� ���������� ����� �������.
	 * 
	 * @param tableName
	 * @return
	 */
	public long getRowsCount(String tableName) {
		long rows = DatabaseUtils.queryNumEntries(sqliteDb, tableName);
		return rows;
	}

	/**
	 * ������� ���������� ��������� �������.
	 * 
	 * @param tableName
	 * @return
	 */
	public int clearTable(String tableName) {
		int affected = sqliteDb.delete(tableName, "1", null);
		return affected;
	}

	/**
	 * ��������� ����������� ���������� �� ����� ������� ���������� � ������.
	 * 
	 * @param receiverTableName
	 *            - ��� �������-�������� ������.
	 * @param source
	 *            - ������, ����������� �� ������-��������..
	 * @return
	 */
	public long copyDocRow(String receiverTableName, Cursor source) {

		int numIdx = source.getColumnIndex(FIELD_DOC_NUM_NAME);
		int dateIdx = source.getColumnIndex(FIELD_DOC_DATE_NAME);
		int commentIdx = source.getColumnIndex(FIELD_DOC_COMMENT_NAME);
		int docRowsIdx = source.getColumnIndex(FIELD_DOC_ROWS_NAME);
		int storeCodeIdx = source.getColumnIndex(FIELD_STORE_CODE_NAME);
		int storeIdx = source.getColumnIndex(FIELD_STORE_DESCR_NAME);
		int docId_Idx = source.getColumnIndex(FIELD_DOC_ID_NAME);

		String num = source.getString(numIdx);
		long date = source.getLong(dateIdx);
		String comment = source.getString(commentIdx);
		int rows = source.getInt(docRowsIdx);
		String storeCode = source.getString(storeCodeIdx);
		String store = source.getString(storeIdx);
		String docId = source.getString(docId_Idx);

		ContentValues docValues = new ContentValues();

		docValues.put(FIELD_DOC_NUM_NAME, num);
		docValues.put(FIELD_DOC_DATE_NAME, date);
		docValues.put(FIELD_DOC_COMMENT_NAME, comment);
		docValues.put(FIELD_DOC_ROWS_NAME, rows);
		docValues.put(FIELD_STORE_CODE_NAME, storeCode);
		docValues.put(FIELD_STORE_DESCR_NAME, store);
		docValues.put(FIELD_DOC_ID_NAME, docId);

		long rowId = 0;
		try {
			rowId = insert(sqliteDb, receiverTableName, docValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rowId;
	}

	/**
	 * ��������� ����������� ���������� �� ����� ������� ������������ � ������.
	 * 
	 * @param receiverTableName
	 *            - ��� �������-�������� ������.
	 * @param source
	 *            - ������, ����������� �� ������-��������..
	 * @return
	 */
	public long copyItemRow(String receiverTableName, Cursor source) {

		int docId_Idx = source.getColumnIndex(FIELD_DOC_ID_NAME);
		int rowNum_Idx = source.getColumnIndex(FIELD_ROW_NUM_NAME);
		int itemCode_Idx = source.getColumnIndex(FIELD_ITEM_CODE_NAME);
		int itemDescr_Idx = source.getColumnIndex(FIELD_ITEM_DESCR_NAME);
		int itemDescrFull_Idx = source
				.getColumnIndex(FIELD_ITEM_DESCR_FULL_NAME);
		int itemUseSpecif_Idx = source
				.getColumnIndex(FIELD_ITEM_USE_SPECIF_NAME);
		int specifCode_Idx = source.getColumnIndex(FIELD_SPECIF_CODE_NAME);
		int specifDescr_Idx = source.getColumnIndex(FIELD_SPECIF_DESCR_NAME);
		int measurDescr_Idx = source.getColumnIndex(FIELD_MEASUR_DESCR_NAME);
		int price_Idx = source.getColumnIndex(FIELD_PRICE_NAME);
		int quantAcc_Idx = source.getColumnIndex(FIELD_QUANT_ACC_NAME);
		int quant_Idx = source.getColumnIndex(FIELD_QUANT_NAME);

		String docId = source.getString(docId_Idx);
		int rowNum = source.getInt(rowNum_Idx);
		String itemCode = source.getString(itemCode_Idx);
		String itemDescr = source.getString(itemDescr_Idx);
		String itemDescrFull = source.getString(itemDescrFull_Idx);
		int itemUseSpecif = source.getInt(itemUseSpecif_Idx);
		String specifCode = source.getString(specifCode_Idx);
		String specifDescr = source.getString(specifDescr_Idx);
		String measurDescr = source.getString(measurDescr_Idx);
		float price = source.getFloat(price_Idx);
		float quantAcc = source.getFloat(quantAcc_Idx);
		float quant = source.getFloat(quant_Idx);

		ContentValues itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, docId);
		itemValues.put(FIELD_ROW_NUM_NAME, rowNum);
		itemValues.put(FIELD_ITEM_CODE_NAME, itemCode);
		itemValues.put(FIELD_ITEM_DESCR_NAME, itemDescr);
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, itemDescrFull);
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, itemUseSpecif);
		itemValues.put(FIELD_SPECIF_CODE_NAME, specifCode);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, specifDescr);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, measurDescr);
		itemValues.put(FIELD_PRICE_NAME, price);
		itemValues.put(FIELD_QUANT_ACC_NAME, quantAcc);
		itemValues.put(FIELD_QUANT_NAME, quant);

		long rowId = 0;
		try {
			rowId = insert(sqliteDb, receiverTableName, itemValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rowId;
	}

	/**
	 * ������������ �������������� ���������.
	 */
	public static String getDocId(String docNum, String docDate) {
		return docDate.concat(docNum);
	}

	/**
	 * ������������ �������������� ���������.
	 */
	public static String getDocId(String docNum, long docDate) {

		String docDateString = dateIDFormatter.format(docDate);
		return getDocId(docNum, docDateString);
	}

	/**
	 * ��������� �� ���������������� ������� ������������.
	 * 
	 * @throws ParseException
	 */
	public void insertDemoItems(SQLiteDatabase dataBase) {

		// ///////////////////////////////////////
		// ������� ����-����������.
		ContentValues docsValues = new ContentValues();

		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		try {

			docsValues.put(FIELD_DOC_NUM_NAME, "���00000010");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2014-12-30 12:06:41").getTime());
			docsValues.put(FIELD_STORE_CODE_NAME, "1");
			docsValues.put(FIELD_STORE_DESCR_NAME, "1 �����");
			docsValues.put(FIELD_DOC_COMMENT_NAME, "������ ��� �������������.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			docsValues.put(
					FIELD_DOC_ID_NAME,
					getDocId(docsValues.getAsString(FIELD_DOC_NUM_NAME),
							docsValues.getAsLong(FIELD_DOC_DATE_NAME)));
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

			docsValues.put(FIELD_DOC_NUM_NAME, "���00000152");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2014-12-31 09:03:11").getTime());
			docsValues.put(FIELD_STORE_CODE_NAME, "25");
			docsValues.put(FIELD_STORE_DESCR_NAME, "25 �����");
			docsValues
					.put(FIELD_DOC_COMMENT_NAME, "������ � ����������������.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			docsValues.put(
					FIELD_DOC_ID_NAME,
					getDocId(docsValues.getAsString(FIELD_DOC_NUM_NAME),
							docsValues.getAsLong(FIELD_DOC_DATE_NAME)));
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

			docsValues.put(FIELD_DOC_NUM_NAME, "���00000003");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2015-01-04 18:25:39").getTime());
			docsValues.put(FIELD_STORE_DESCR_NAME, "106 �����");
			docsValues.put(FIELD_STORE_CODE_NAME, "106");
			docsValues.put(FIELD_DOC_COMMENT_NAME,
					"������ � ���������������� � ���.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			docsValues.put(
					FIELD_DOC_ID_NAME,
					getDocId(docsValues.getAsString(FIELD_DOC_NUM_NAME),
							docsValues.getAsLong(FIELD_DOC_DATE_NAME)));
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

			docsValues.put(FIELD_DOC_NUM_NAME, "���00000004");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2015-01-05 01:00:01").getTime());
			docsValues.put(FIELD_STORE_CODE_NAME, "204");
			docsValues.put(FIELD_STORE_DESCR_NAME, "204 �����");
			docsValues.put(FIELD_DOC_COMMENT_NAME, "����.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			docsValues.put(
					FIELD_DOC_ID_NAME,
					getDocId(docsValues.getAsString(FIELD_DOC_NUM_NAME),
							docsValues.getAsLong(FIELD_DOC_DATE_NAME)));
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// ///////////////////////////////////////
		// ������� ������������ ����-����������.
		ContentValues itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�7032");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "*����� ������������ �5 ��2");
		itemValues
				.put(FIELD_ITEM_DESCR_FULL_NAME, "*����� ������������ �5 ��2");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 10);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�1118");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "������������� ���. ���� ��");
		itemValues
				.put(FIELD_ITEM_DESCR_FULL_NAME, "������������� ���. ���� ��");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 62);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�5418");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "������ �/����� 16�");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "������ �/����� 16�");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27522");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�� ����� ����������� ��������� �������� *600");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"����� ���������. ��.��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 18.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 60);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27522");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�� ����� ����������� ��������� �������� *600");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"����� ���������. ��.��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 20);
		itemValues.put(FIELD_QUANT_ACC_NAME, 120);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27522");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�� ����� ����������� ��������� �������� *600");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"����� ���������. ��.��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 21);
		itemValues.put(FIELD_QUANT_ACC_NAME, 30);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�2554");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "����� ����� �1/2 �����.");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "����� �����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 15);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�5799");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "�14 ��������� ������� �� �����");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�14 ��������� ������� �� �����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 22);
		itemValues.put(FIELD_QUANT_ACC_NAME, 11);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "��0179");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�15 �������� ������ ��. 100 ���� (�����)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�������� ��. 100 ���� (�����)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 150);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�8687");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"��. ���������� ���� ��������� ����");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "��. ���������� ����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 117);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�7726");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "��.���������� ���� ����� �����");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "��. ���������� ����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 117);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�7727");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "��.���������� ���� �������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "��. ���������� ����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 117);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�6595");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "��.���.����� Low");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "��.���.����� Low");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 90);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�6592");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "��.���.����� High");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "��.���.����� High");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 90);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41���00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�6590");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "��.���.����� Med");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "��.���.����� Med");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 90);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0795");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "����� ���������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "����� ���������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 52081);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "6/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 25);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0795");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "����� ���������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "����� ���������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 53635);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "7/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 25);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0795");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "����� ���������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "����� ���������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 55092);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "8/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 25);
		itemValues.put(FIELD_QUANT_ACC_NAME, 9);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0797");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "����� � ������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "����� � ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 55898);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "8/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 19);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0718");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"���������� �� ��������  2012 ���������� 777");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"���������� �� ��������  2012 ���������� 777");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 41519);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "1/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0718");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"���������� �� ��������  2012 ���������� 777");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"���������� �� ��������  2012 ���������� 777");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 41519);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "1/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 23.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0718");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"���������� �� ��������  2012 ���������� 777");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"���������� �� ��������  2012 ���������� 777");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 51231);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 44079);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "5/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 35);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 55185);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "15/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 37);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 56245);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "16/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 37);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "�0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ��������� ���������� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 42999);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "4/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 33);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 39501);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "1/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 16792);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "4/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 20);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 13375);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 20);
		itemValues.put(FIELD_QUANT_ACC_NAME, 6);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11���00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������� � ���������. ����������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 31924);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 22152);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 52847);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "19/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 25134);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "7/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 25134);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "7/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 18);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 ��������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 41654);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "4/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28301");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�� ����� �������� ������� ���������������.50�");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�� ����� �������� ������� ���������������.50�");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 55);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28307");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�� ������� �������� ������������� �������  �����30");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�� ������� �������� �������������������� �������  �����30");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 120);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28555");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"������ ������� � ������ 70�� 10�� *20");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������ ������� � ������ 70��");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 24);
		itemValues.put(FIELD_QUANT_ACC_NAME, 35);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28555");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"������ ������� � ������ 70�� 10�� *20");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"������ ������� � ������ 70��");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 9);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28130");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"��� ���� ���� ��� ��� 68� 4*24=96  �");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "���.��� ��� ���� ���� 68�");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 34);
		itemValues.put(FIELD_QUANT_ACC_NAME, 831);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 41778);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "8/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 3714);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "6/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 39098);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "52/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 36448);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "45/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39���00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"�������� ������                        (���������)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 1637);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 33.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 24);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 34.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 495);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 35.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 48);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 44);
		itemValues.put(FIELD_QUANT_ACC_NAME, 120);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 48);
		itemValues.put(FIELD_QUANT_ACC_NAME, 9);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 ������ �������  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 49);
		itemValues.put(FIELD_QUANT_ACC_NAME, 36);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ����  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ����-����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 44);
		itemValues.put(FIELD_QUANT_ACC_NAME, 149);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ����  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ����-����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 53);
		itemValues.put(FIELD_QUANT_ACC_NAME, 258);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ����  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ����-����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 56);
		itemValues.put(FIELD_QUANT_ACC_NAME, 300);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ����  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ����-����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 60);
		itemValues.put(FIELD_QUANT_ACC_NAME, 96);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ����  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ����-����");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 61);
		itemValues.put(FIELD_QUANT_ACC_NAME, 72);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ������  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 44);
		itemValues.put(FIELD_QUANT_ACC_NAME, 24);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ������  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 56);
		itemValues.put(FIELD_QUANT_ACC_NAME, 132);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ������  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 60);
		itemValues.put(FIELD_QUANT_ACC_NAME, 24);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01���00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1� ������  �������  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1� ������");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "��");
		itemValues.put(FIELD_PRICE_NAME, 61);
		itemValues.put(FIELD_QUANT_ACC_NAME, 12);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);
	}
}