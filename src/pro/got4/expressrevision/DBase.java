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
	public static final String FIELD_DOC_ID_NAME = "doc_id";
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

	// Форматтер даты для преобразования в формат, понятный пользователю.
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss", Locale.getDefault());

	// Индексное поле, используемое адаптерами для идентификации строки.
	// ИЗМЕНЯТЬ ЕГО ИМЯ НЕЛЬЗЯ!!!
	public static final String FIELD_ID_NAME = "_id";

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
	public long update(SQLiteDatabase dataBase, String tableName, int row_id,
			ContentValues values) {

		String[] whereArgs = { String.valueOf(row_id) };
		int rowsAffected = dataBase.update(tableName, values, FIELD_ID_NAME
				+ " = ?", whereArgs);

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
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

			// Таблица, хранящая реальный набор документов.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_NAME + " (" + FIELD_ID_NAME
					+ " integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " integer, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_DOC_ROWS_NAME + " integer, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

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
					+ FIELD_QUANT_NAME + " real, " + FIELD_INDEX_NAME
					+ " text);");

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
					+ FIELD_QUANT_NAME + " real, " + FIELD_INDEX_NAME
					+ " text);");

			insertDemoItems(db);
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
		if (tableName.equals(TABLE_DOCS_NAME)
		/* || tableName.equals(TABLE_DOCS_DEMO_NAME) */) {

			indexFields = new String[] { FIELD_DOC_NUM_NAME,
					FIELD_DOC_DATE_NAME, FIELD_DOC_COMMENT_NAME,
					FIELD_STORE_DESCR_NAME };

		} else if (tableName.equals(TABLE_ITEMS_NAME)
		/* || tableName.equals(TABLE_ITEMS_DEMO_NAME) */) {

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

		String num = source.getString(numIdx);
		long date = source.getLong(dateIdx);
		String comment = source.getString(commentIdx);
		int rows = source.getInt(docRowsIdx);
		String storeCode = source.getString(storeCodeIdx);
		String store = source.getString(storeIdx);

		ContentValues docValues = new ContentValues();

		docValues.put(FIELD_DOC_NUM_NAME, num);
		docValues.put(FIELD_DOC_DATE_NAME, date);
		docValues.put(FIELD_DOC_COMMENT_NAME, comment);
		docValues.put(FIELD_DOC_ROWS_NAME, rows);
		docValues.put(FIELD_STORE_CODE_NAME, storeCode);
		docValues.put(FIELD_STORE_DESCR_NAME, store);

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
	 * Заполняет БД демонстрационным набором номенклатуры.
	 * 
	 * @throws ParseException
	 */
	public void insertDemoItems(SQLiteDatabase dataBase) {

		// ///////////////////////////////////////
		// Таблица демо-документов.
		ContentValues docsValues = new ContentValues();

		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		try {

			docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС00000010");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2014-12-30 12:06:41").getTime());
			docsValues.put(FIELD_STORE_CODE_NAME, "1");
			docsValues.put(FIELD_STORE_DESCR_NAME, "1 киоск");
			docsValues.put(FIELD_DOC_COMMENT_NAME, "Товары без характеристик.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

			docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС00000152");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2014-12-31 09:03:11").getTime());
			docsValues.put(FIELD_STORE_CODE_NAME, "25");
			docsValues.put(FIELD_STORE_DESCR_NAME, "25 киоск");
			docsValues
					.put(FIELD_DOC_COMMENT_NAME, "Товары с характеристиками.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

			docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС00000003");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2015-01-04 18:25:39").getTime());
			docsValues.put(FIELD_STORE_DESCR_NAME, "106 киоск");
			docsValues.put(FIELD_STORE_CODE_NAME, "106");
			docsValues.put(FIELD_DOC_COMMENT_NAME,
					"Товары с характеристиками и без.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

			docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС00000004");
			docsValues.put(FIELD_DOC_DATE_NAME,
					formatter.parse("2015-01-05 01:00:01").getTime());
			docsValues.put(FIELD_STORE_CODE_NAME, "204");
			docsValues.put(FIELD_STORE_DESCR_NAME, "204 киоск");
			docsValues.put(FIELD_DOC_COMMENT_NAME, "Кола.");
			docsValues.put(FIELD_DOC_ROWS_NAME, 15);
			insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// ///////////////////////////////////////
		// Таблица номенклатуры демо-документов.
		ContentValues itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я7032");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "*бланк Доверенность А5 №М2");
		itemValues
				.put(FIELD_ITEM_DESCR_FULL_NAME, "*бланк Доверенность А5 №М2");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 10);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я1118");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Автодокументы иск. кожа РП");
		itemValues
				.put(FIELD_ITEM_DESCR_FULL_NAME, "Автодокументы иск. кожа РП");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 62);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я5418");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Альбом д/рисов 16л");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Альбом д/рисов 16л");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27522");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"жр Орбит белоснежный фруктовый коктейль *600");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Орбит белоснежн. фр.коктейль");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 18.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 60);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27522");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"жр Орбит белоснежный фруктовый коктейль *600");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Орбит белоснежн. фр.коктейль");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 20);
		itemValues.put(FIELD_QUANT_ACC_NAME, 120);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27522");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"жр Орбит белоснежный фруктовый коктейль *600");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Орбит белоснежн. фр.коктейль");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 21);
		itemValues.put(FIELD_QUANT_ACC_NAME, 30);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я2554");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Кисть белка №1/2 кругл.");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Кисть белка");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 15);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я5799");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Н14 Украшение Бабочка на клипе");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Н14 Украшение Бабочка на клипе");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 22);
		itemValues.put(FIELD_QUANT_ACC_NAME, 11);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "яа0179");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Н15 Гирлянда свечки эл. 100 ламп (белый)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Гирлянда эл. 100 ламп (белый)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 150);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я8687");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Эл. Испаритель Понс Ароматный кофе");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Эл. Испаритель Понс");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 117);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я7726");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Эл.Испаритель Понс Дикая вишня");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Эл. Испаритель Понс");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 117);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я7727");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Эл.Испаритель Понс Классик");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Эл. Испаритель Понс");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 117);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я6595");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "эл.сиг.Вишня Low");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "эл.сиг.Вишня Low");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 90);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я6592");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "эл.сиг.Табак High");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "эл.сиг.Табак High");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 90);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-30 12:06:41ЭКС00000010");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "я6590");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "эл.сиг.Табак Med");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "эл.сиг.Табак Med");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 90);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0795");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Дарья БИОГРАФИЯ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Дарья БИОГРАФИЯ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 52081);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "6/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 25);
		itemValues.put(FIELD_QUANT_ACC_NAME, 5);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0795");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Дарья БИОГРАФИЯ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Дарья БИОГРАФИЯ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 53635);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "7/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 25);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0795");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Дарья БИОГРАФИЯ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Дарья БИОГРАФИЯ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 55092);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "8/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 25);
		itemValues.put(FIELD_QUANT_ACC_NAME, 9);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0797");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "Жизнь и любовь");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "Жизнь и любовь");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 55898);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "8/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 19);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0718");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"КРОССВОРДЫ от Потапыча  2012 Спецвыпуск 777");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"КРОССВОРДЫ от Потапыча  2012 Спецвыпуск 777");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 41519);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "1/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0718");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"КРОССВОРДЫ от Потапыча  2012 Спецвыпуск 777");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"КРОССВОРДЫ от Потапыча  2012 Спецвыпуск 777");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 55908);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "4/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0718");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"КРОССВОРДЫ от Потапыча  2012 Спецвыпуск 777");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"КРОССВОРДЫ от Потапыча  2012 Спецвыпуск 777");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 51231);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 44079);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "5/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 35);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 55185);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "15/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 37);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 56245);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "16/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 37);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "Ж0725");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"ОБЕРЕГИ и ТАЛИСМАНЫ Спецвыпуск газеты");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 42999);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "4/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 33);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 39501);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "1/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 16792);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "4/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 20);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 13375);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 20);
		itemValues.put(FIELD_QUANT_ACC_NAME, 6);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2014-12-31 09:03:11ЭКС00000152");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "6294");
		itemValues
				.put(FIELD_ITEM_DESCR_NAME, "Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Садовод и огородник. Спецвыпуск");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 31924);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 22152);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 52847);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "19/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 25134);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "7/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 38988);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "26/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "1623");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1000 СЕКРЕТОВ");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 41654);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "4/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 16);
		itemValues.put(FIELD_QUANT_ACC_NAME, 4);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28301");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"ав драже Виталайф Калинка витаминизирован.50г");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"ав драже Виталайф Калинка витаминизирован.50г");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 55);
		itemValues.put(FIELD_QUANT_ACC_NAME, 2);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28307");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"ав напиток Виталайф черноплодноря витамин  сухой30");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"ав напиток Виталайф черноплоднорябиновый витамин  сухой30");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 120);
		itemValues.put(FIELD_QUANT_ACC_NAME, 1);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28555");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Арахис холодец с хреном 70гр 10шт *20");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Арахис холодец с хреном 70гр");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 24);
		itemValues.put(FIELD_QUANT_ACC_NAME, 35);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28555");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Арахис холодец с хреном 70гр 10шт *20");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Арахис холодец с хреном 70гр");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 23);
		itemValues.put(FIELD_QUANT_ACC_NAME, 9);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "28130");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"бат КИНГ сайз Кит Кат 68г 4*24=96  М");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "бат.Кит Кат кинг сайз 68г");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 34);
		itemValues.put(FIELD_QUANT_ACC_NAME, 831);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 41778);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "8/14");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 3714);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "6/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 39098);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "52/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 36448);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "45/13");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-04 18:25:39ЭКС00000003");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "8596");
		itemValues.put(FIELD_ITEM_DESCR_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME,
				"Экспресс газета                        (Роспечать)");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 1);
		itemValues.put(FIELD_SPECIF_CODE_NAME, 1637);
		itemValues.put(FIELD_SPECIF_DESCR_NAME, "3/12");
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 32);
		itemValues.put(FIELD_QUANT_ACC_NAME, 3);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 1);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 33.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 24);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 2);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 34.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 495);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 3);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 35.5);
		itemValues.put(FIELD_QUANT_ACC_NAME, 48);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 4);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 44);
		itemValues.put(FIELD_QUANT_ACC_NAME, 120);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 5);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 48);
		itemValues.put(FIELD_QUANT_ACC_NAME, 9);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 6);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27117");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "0,5 спрайт бутылка  *24");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 49);
		itemValues.put(FIELD_QUANT_ACC_NAME, 36);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 7);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л кока  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л кока-кола");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 44);
		itemValues.put(FIELD_QUANT_ACC_NAME, 149);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 8);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л кока  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л кока-кола");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 53);
		itemValues.put(FIELD_QUANT_ACC_NAME, 258);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 9);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л кока  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л кока-кола");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 56);
		itemValues.put(FIELD_QUANT_ACC_NAME, 300);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 10);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л кока  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л кока-кола");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 60);
		itemValues.put(FIELD_QUANT_ACC_NAME, 96);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 11);
		itemValues.put(FIELD_ITEM_CODE_NAME, "26997");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л кока  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л кока-кола");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 61);
		itemValues.put(FIELD_QUANT_ACC_NAME, 72);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 12);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л спрайт  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л спрайт");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 44);
		itemValues.put(FIELD_QUANT_ACC_NAME, 24);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 13);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л спрайт  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л спрайт");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 56);
		itemValues.put(FIELD_QUANT_ACC_NAME, 132);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 14);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л спрайт  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л спрайт");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 60);
		itemValues.put(FIELD_QUANT_ACC_NAME, 24);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);

		itemValues = new ContentValues();
		itemValues.put(FIELD_DOC_ID_NAME, "2015-01-05 01:00:01ЭКС00000004");
		itemValues.put(FIELD_ROW_NUM_NAME, 15);
		itemValues.put(FIELD_ITEM_CODE_NAME, "27114");
		itemValues.put(FIELD_ITEM_DESCR_NAME, "1л спрайт  бутылка  *12");
		itemValues.put(FIELD_ITEM_DESCR_FULL_NAME, "1л спрайт");
		itemValues.put(FIELD_ITEM_USE_SPECIF_NAME, 0);
		itemValues.put(FIELD_MEASUR_DESCR_NAME, "шт");
		itemValues.put(FIELD_PRICE_NAME, 61);
		itemValues.put(FIELD_QUANT_ACC_NAME, 12);
		itemValues.put(FIELD_QUANT_NAME, 0);
		insert(dataBase, TABLE_ITEMS_DEMO_NAME, itemValues);
	}
}