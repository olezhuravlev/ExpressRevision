package pro.got4.expressrevision;

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

	public long insert(String tableName, ContentValues values) {
		return sqliteDb.insert(tableName, null, values);
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
			db.execSQL("CREATE TABLE " + TABLE_DOCS_DEMO_NAME
					+ " (_id integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " text, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

			// Таблица, хранящая реальный набор документов.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_NAME
					+ " (_id integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " text, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

			// Создание таблиц номенклатуры загруженных документов.
			// Таблица, хранящая набор номенклатуры для демонстрационных
			// документов.
			db.execSQL("CREATE TABLE " + TABLE_ITEMS_DEMO_NAME
					+ " (_id integer primary key autoincrement, "
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
			db.execSQL("CREATE TABLE " + TABLE_ITEMS_NAME
					+ " (_id integer primary key autoincrement, "
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
	 * Дополняет коллекцию ContentValues индексным полем. Индексное поле
	 * добавляется только в том случае, если его в коллекции еще нет.
	 * 
	 * @param values
	 * @return String, которой было заполнено индексное поле.
	 */
	public String addItemIndexField(String tableName, ContentValues values) {

		String indexValue = (String) values.get(FIELD_INDEX_NAME);

		if (indexValue != null) // TODO Проверить, что работает!
			return indexValue;

		String[] indexFields = null;
		if (tableName == TABLE_DOCS_NAME) {

			indexFields = new String[] { FIELD_DOC_NUM_NAME,
					FIELD_DOC_DATE_NAME, FIELD_DOC_COMMENT_NAME,
					FIELD_STORE_DESCR_NAME };

		} else if (tableName == TABLE_ITEMS_NAME) {

			indexFields = new String[] { FIELD_ITEM_CODE_NAME,
					FIELD_ITEM_DESCR_NAME, FIELD_ITEM_DESCR_FULL_NAME,
					FIELD_SPECIF_DESCR_NAME };

		}

		indexValue = getContentValuesString(values, indexFields, true)
				.toLowerCase(Locale.getDefault());

		values.put(FIELD_INDEX_NAME, indexValue);

		return indexValue;
	}

	/**
	 * Возвращает содержимое коллекции ContentValues в виде строки.
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

			String current = values.getAsString(keys[i]);
			if (uniqueOnly) {
				if (result.indexOf(current) == -1) {
					result += current;
				}
			} else {
				result += current;
			}
		}

		return result;
	}

	/**
	 * Заполняет БД демонстрационным набором номенклатуры.
	 */
	public void insertDemoItems(SQLiteDatabase dataBase) {

		// ///////////////////////////////////////
		// Таблица списка документов.
		ContentValues docsValues = new ContentValues();

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005264");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-05 13:28:15");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "Для Гули.");
		docsValues.put(FIELD_STORE_CODE_NAME, "1");
		docsValues.put(FIELD_STORE_DESCR_NAME, "1 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005325");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-05 09:08:22");
		docsValues.put(FIELD_STORE_DESCR_NAME, "2 киоск");
		docsValues.put(FIELD_STORE_CODE_NAME, "2");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "Настя можно делать!");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005368");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-06");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "135");
		docsValues.put(FIELD_STORE_DESCR_NAME, "135 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005369");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "Возьмите расписку с киоскера!");
		docsValues.put(FIELD_STORE_CODE_NAME, "95");
		docsValues.put(FIELD_STORE_DESCR_NAME, "95 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005401");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME,
				"Маша, не разбавляй сметану, я уже разбавила.");
		docsValues.put(FIELD_STORE_CODE_NAME, "151");
		docsValues.put(FIELD_STORE_DESCR_NAME, "151 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005402");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "152");
		docsValues.put(FIELD_STORE_DESCR_NAME, "152 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005403");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "153");
		docsValues.put(FIELD_STORE_DESCR_NAME, "153 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005404");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "154");
		docsValues.put(FIELD_STORE_DESCR_NAME, "154 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005405");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "Срочно!");
		docsValues.put(FIELD_STORE_CODE_NAME, "155");
		docsValues.put(FIELD_STORE_DESCR_NAME, "155 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005406");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "156");
		docsValues.put(FIELD_STORE_DESCR_NAME, "156 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005407");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "157");
		docsValues.put(FIELD_STORE_DESCR_NAME, "157 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005408");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "158");
		docsValues.put(FIELD_STORE_DESCR_NAME, "158 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005409");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "159");
		docsValues.put(FIELD_STORE_DESCR_NAME, "159 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005410");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "160");
		docsValues.put(FIELD_STORE_DESCR_NAME, "160 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005411");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "161");
		docsValues.put(FIELD_STORE_DESCR_NAME, "161 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "ЭКС0005412");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "162");
		docsValues.put(FIELD_STORE_DESCR_NAME, "162 киоск");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		// ///////////////////////////////////////
		// Таблицы загруженных документов.
		// ContentValues loadedDocsValues = new ContentValues();
		// loadedDocsValues.put(FIELD_CODE_NAME, 1);
		// loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Натс");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "шоколадный батончик Nuts");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 2);
		// loadedDocsValues.put(FIELD_NAME_NAME, "гор.шок.Бабай 75");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "горький шоколад Бабаевский 75%");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 3);
		// loadedDocsValues.put(FIELD_NAME_NAME, "сиг.Друг");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "сигареты Друг");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 4);
		// loadedDocsValues.put(FIELD_NAME_NAME, "газ. Кр.Зв.");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "газета Красная Звезда");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 5);
		// loadedDocsValues.put(FIELD_NAME_NAME, "газ. Мол.Сиб.");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "газета Молодость Сибири");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 6);
		// loadedDocsValues.put(FIELD_NAME_NAME, "газ. Комс.Прав.");
		// loadedDocsValues
		// .put(FIELD_NAMEFULL_NAME, "газета Комсомольская Правда");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 7);
		// loadedDocsValues.put(FIELD_NAME_NAME, "жур. НиЖ");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "журнал Наука и жизнь");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "ж");
		// loadedDocsValues.put(FIELD_NAME_NAME, "жур. Mens Health");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "журнал Mens Health");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "Б97");
		// loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Сникрс 125");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "шоколадный батончик Сникерс, 125 гр.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "Б101");
		// loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Баунти 125");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "шоколадный батончик Баунти, 125 гр.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 8);
		// loadedDocsValues.put(FIELD_NAME_NAME, "газ.нап.Фанта");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "газированный напиток Фанта");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 9);
		// loadedDocsValues.put(FIELD_NAME_NAME, "газ.нап.Тархун");
		// loadedDocsValues
		// .put(FIELD_NAMEFULL_NAME, "газированный напиток Тархун");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 1001);
		// loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Сникрс 200");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "шоколадный батончик Сникерс, 200 гр.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, ".Бат");
		// loadedDocsValues.put(FIELD_NAME_NAME, "ж.р.Дирол");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "жевательная резинка Дирол");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 45006);
		// loadedDocsValues.put(FIELD_NAME_NAME, "газ.Веч.Нск.");
		// loadedDocsValues
		// .put(FIELD_NAMEFULL_NAME, "газета Вечерний Новосибирск");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 630005);
		// loadedDocsValues.put(FIELD_NAME_NAME, "жур. Поп.Мех.");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "журнал Популярная Механика");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 11);
		// loadedDocsValues.put(FIELD_NAME_NAME, "стир.рез.Кохинор");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "стирательная резинка Kohinoor");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 12);
		// loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Баунти 200");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "шоколадный батончик Баунти, 200 гр.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 14);
		// loadedDocsValues.put(FIELD_NAME_NAME, "газ.нап.К-Кола 330 ж/б");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "газированный напиток Кока-кола, 330гр. жест.б.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "_ш07");
		// loadedDocsValues.put(FIELD_NAME_NAME, "шок.нап.Nesquick");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "шоколадный напиток Несквик");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "995");
		// loadedDocsValues.put(FIELD_NAME_NAME, "ватн.палочки Идеал");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "ватные палочки Идеал");

		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);

	}

	/**
	 * Возвращает курсор на всё содержимое таблицы.
	 * 
	 * @return
	 */
	public Cursor getAllRows(String tableName) {
		return sqliteDb.query(tableName, null, null, null, null, null, null);
	}

	/**
	 * Возвращает количество строк таблицы.
	 * 
	 * @param tableName
	 * @return
	 */
	public long getRowsCount(String tableName) {
		return DatabaseUtils.queryNumEntries(sqliteDb, tableName);
	}

	/**
	 * Очищает содержимое указанной таблицы.
	 * 
	 * @param tableName
	 * @return
	 */
	public int clearTable(String tableName) {
		return sqliteDb.delete(tableName, "1", null);
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
		int storeCodeIdx = source.getColumnIndex(FIELD_STORE_CODE_NAME);
		int storeIdx = source.getColumnIndex(FIELD_STORE_DESCR_NAME);

		String num = source.getString(numIdx);
		String date = source.getString(dateIdx);
		String comment = source.getString(commentIdx);
		String storeCode = source.getString(storeCodeIdx);
		String store = source.getString(storeIdx);

		ContentValues docValues = new ContentValues();

		docValues.put(FIELD_DOC_NUM_NAME, num);
		docValues.put(FIELD_DOC_DATE_NAME, date);
		docValues.put(FIELD_DOC_COMMENT_NAME, comment);
		docValues.put(FIELD_STORE_CODE_NAME, storeCode);
		docValues.put(FIELD_STORE_DESCR_NAME, store);

		long rowId = insert(sqliteDb, receiverTableName, docValues);

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

		long rowId = insert(sqliteDb, receiverTableName, itemValues);

		return rowId;
	}
}