package pro.got4.expressrevision;

import java.text.SimpleDateFormat;
import java.util.Locale;

import pro.got4.library.ExpressRevision_DemoItems;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBase {

	public static final String DB_NAME = "ExpressRevision";

	// Таблицы документов.
	public static final String TABLE_DOCS_DEMO_NAME = "documents_demo";
	public static final String TABLE_DOCS_NAME = "documents";

	// Поля таблиц документов.
	public static final String FIELD_DOC_NUM_NAME = "doc_num";
	public static final String FIELD_DOC_DATE_NAME = "doc_date";
	public static final String FIELD_DOC_COMMENT_NAME = "doc_comm";
	public static final String FIELD_DOC_ROWS_NAME = "doc_rows";
	public static final String FIELD_STORE_CODE_NAME = "store_code";
	public static final String FIELD_STORE_DESCR_NAME = "store_descr";

	// Таблицы номенклатуры загруженных документов.
	public static final String TABLE_ITEMS_DEMO_NAME = "items_demo";
	public static final String TABLE_ITEMS_NAME = "items";

	// Поля таблиц номенклатуры загруженных документов.
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

	// Поле идентификатора документов, присутствующее и в таблице документов, и
	// в таблице номенклатуры.
	public static final String FIELD_DOC_ID_NAME = "doc_id";

	// Индексное поле, используемое адаптерами для идентификации строки.
	// ИЗМЕНЯТЬ ЕГО ИМЯ НЕЛЬЗЯ!!!
	public static final String FIELD_ID_NAME = "_id";

	// Форматтер даты для преобразования в формат, понятный пользователю.
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss", Locale.getDefault());

	// Форматтер даты для приведения даты к формату, используемому в
	// идентификаторе документа.
	public static final SimpleDateFormat dateIDFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	// Индексное поле, используемое для поиска в таблицах.
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
	 * Добавление записи в БД. В зависимости от таблицы используется разный
	 * набор полей, формирующих поле поиска.
	 * 
	 * @param dataBase
	 * @param tableName
	 * @param values
	 * @return
	 */
	// Эта версия используется при первоначальной инициализации БД.
	public long insert(SQLiteDatabase dataBase, String tableName,
			ContentValues values) {

		// Добавление поля, используемого для поиска.
		addItemIndexField(tableName, values);

		return dataBase.insert(tableName, null, values);
	}

	/**
	 * Обновляет поля строки с указанным идентификатором.
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
	 * Обновляет поля строки с указанным идентификатором.
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

			// Создание таблиц списка документов.
			// Таблица, хранящая набор документов для демо-режима.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_DEMO_NAME + " ("
					+ FIELD_ID_NAME + " integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " integer, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_DOC_ROWS_NAME + " integer, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_DOC_ID_NAME
					+ " text," + FIELD_INDEX_NAME + " text);");

			// Таблица, хранящая реальный набор документов.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_NAME + " (" + FIELD_ID_NAME
					+ " integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " integer, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_DOC_ROWS_NAME + " integer, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_DOC_ID_NAME
					+ " text," + FIELD_INDEX_NAME + " text);");

			// Создание таблиц номенклатуры загруженных документов.
			// Таблица, хранящая набор номенклатуры для демонстрационных
			// документов.
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

			// Таблица, хранящая набор номенклатуры для реальных документов.
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

			// //////////////////////////////////////
			// Заполнение таблиц демо-данных.

			// Таблицы документов.
			ExpressRevision_DemoItems.TABLE_DOCS_DEMO_NAME = TABLE_DOCS_DEMO_NAME;
			ExpressRevision_DemoItems.TABLE_DOCS_NAME = TABLE_DOCS_NAME;

			// Индексное поле, используемое для поиска в таблицах.
			ExpressRevision_DemoItems.FIELD_INDEX_NAME = FIELD_INDEX_NAME;

			// Поля таблиц документов.
			ExpressRevision_DemoItems.FIELD_DOC_ID_NAME = FIELD_DOC_ID_NAME;
			ExpressRevision_DemoItems.FIELD_DOC_NUM_NAME = FIELD_DOC_NUM_NAME;
			ExpressRevision_DemoItems.FIELD_DOC_DATE_NAME = FIELD_DOC_DATE_NAME;
			ExpressRevision_DemoItems.FIELD_DOC_COMMENT_NAME = FIELD_DOC_COMMENT_NAME;
			ExpressRevision_DemoItems.FIELD_DOC_ROWS_NAME = FIELD_DOC_ROWS_NAME;
			ExpressRevision_DemoItems.FIELD_STORE_CODE_NAME = FIELD_STORE_CODE_NAME;
			ExpressRevision_DemoItems.FIELD_STORE_DESCR_NAME = FIELD_STORE_DESCR_NAME;

			// Таблицы номенклатуры загруженных документов.
			ExpressRevision_DemoItems.TABLE_ITEMS_DEMO_NAME = TABLE_ITEMS_DEMO_NAME;
			ExpressRevision_DemoItems.TABLE_ITEMS_NAME = TABLE_ITEMS_NAME;

			// Поля таблиц номенклатуры загруженных документов.
			ExpressRevision_DemoItems.FIELD_ROW_NUM_NAME = FIELD_ROW_NUM_NAME;
			ExpressRevision_DemoItems.FIELD_ITEM_CODE_NAME = FIELD_ITEM_CODE_NAME;
			ExpressRevision_DemoItems.FIELD_ITEM_DESCR_NAME = FIELD_ITEM_DESCR_NAME;
			ExpressRevision_DemoItems.FIELD_ITEM_DESCR_FULL_NAME = FIELD_ITEM_DESCR_FULL_NAME;
			ExpressRevision_DemoItems.FIELD_ITEM_USE_SPECIF_NAME = FIELD_ITEM_USE_SPECIF_NAME;
			ExpressRevision_DemoItems.FIELD_SPECIF_CODE_NAME = FIELD_SPECIF_CODE_NAME;
			ExpressRevision_DemoItems.FIELD_SPECIF_DESCR_NAME = FIELD_SPECIF_DESCR_NAME;
			ExpressRevision_DemoItems.FIELD_MEASUR_DESCR_NAME = FIELD_MEASUR_DESCR_NAME;
			ExpressRevision_DemoItems.FIELD_PRICE_NAME = FIELD_PRICE_NAME;
			ExpressRevision_DemoItems.FIELD_QUANT_ACC_NAME = FIELD_QUANT_ACC_NAME;
			ExpressRevision_DemoItems.FIELD_QUANT_NAME = FIELD_QUANT_NAME;

			// Непосредственно содержимое заполняется сторонней библиотекой.
			ExpressRevision_DemoItems.installDemoItems(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Будет изменено при обновлении структуры БД.
		}
	}

	/**
	 * Дополняет коллекцию ContentValues индексным полем.<br>
	 * Индексное поле добавляется только в том случае, если его в коллекции еще
	 * нет.
	 * 
	 * @param values
	 * @return String, которой было заполнено индексное поле.
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
	 * Возвращает содержимое коллекции ContentValues в виде строки.<br>
	 * Используется при заполнении поля поиска.
	 * 
	 * @param values
	 * @param keys
	 * @param uniqueOnly
	 *            - флаг того, что в строку нужно добавлять только уникальные
	 *            значения.
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

			// Преобразование даты документа в читаемый формат.
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
	 * Возвращает курсор на строку таблицы номенклатуры с указанным
	 * идентификатором. Курсор сразу установлен на позицию для чтения данных.
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
	 * Возвращает курсор на всё содержимое таблицы.
	 * 
	 * @return
	 */
	public Cursor getRowsAll(String tableName, String orderBy) {
		return sqliteDb.query(tableName, null, null, null, null, null, orderBy);
	}

	/**
	 * Возвращает курсор на содержимое таблицы, отобранное по условию.
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
	 * Возвращает курсор на содержимое таблицы, отобранное по оператору LIKE.
	 */
	public Cursor getRowsFilteredLike(String tableName, String filterString) {

		String filter = FIELD_INDEX_NAME.concat(" LIKE '%")
				.concat(filterString).concat("%'");

		return sqliteDb.query(tableName, null, filter, null, null, null, null);
	}

	/**
	 * Исполняет произвольный запрос к таблице БД.
	 */
	public Cursor getRows(String sql, String[] selectionArgs) {

		return sqliteDb.rawQuery(sql, selectionArgs);
	}

	/**
	 * Возвращает количество строк таблицы.
	 * 
	 * @param tableName
	 * @return
	 */
	public long getRowsCount(String tableName) {
		long rows = DatabaseUtils.queryNumEntries(sqliteDb, tableName);
		return rows;
	}

	/**
	 * Очищает содержимое указанной таблицы.
	 * 
	 * @param tableName
	 * @return
	 */
	public int clearTable(String tableName) {
		int affected = sqliteDb.delete(tableName, "1", null);
		return affected;
	}

	/**
	 * Выполняет копирование содержимое из одной таблицы документов в другую.
	 * 
	 * @param receiverTableName
	 *            - имя таблицы-приёмника данных.
	 * @param source
	 *            - курсор, указывающий на строку-источник..
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
	 * Выполняет копирование содержимое из одной таблицы номенклатуры в другую.
	 * 
	 * @param receiverTableName
	 *            - имя таблицы-приёмника данных.
	 * @param source
	 *            - курсор, указывающий на строку-источник..
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
	 * Формирование идентификатора документа.
	 */
	public static String getDocId(String docNum, String docDate) {
		return docDate.concat(docNum);
	}

	/**
	 * Формирование идентификатора документа.
	 */
	public static String getDocId(String docNum, long docDate) {

		String docDateString = dateIDFormatter.format(docDate);
		return getDocId(docNum, docDateString);
	}
}