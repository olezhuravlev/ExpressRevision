package pro.got4.expressrevision;

import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBase {

	public static final String DB_NAME = "ExpressRevision";

	// Таблица документов.
	public static final String TABLE_DOCS_NAME = "documents";
	public static final String FIELD_REF_NAME = "ref";
	public static final String FIELD_NUM_NAME = "number";
	public static final String FIELD_DATE_NAME = "date";
	public static final String FIELD_STORE_NAME = "store";
	public static final String FIELD_COMMENT_NAME = "comment";

	// Таблица загруженного документа.
	public static final String TABLE_ITEMS_NAME = "items";
	public static final String FIELD_CODE_NAME = "code";
	public static final String FIELD_NAME_NAME = "name";
	public static final String FIELD_NAMEFULL_NAME = "namefull";

	// Индексное поле, используемое для поиска в таблицах.
	public static final String FIELD_INDEX_NAME = "idx";

	private final static int DBVersion = 1;

	Context context;
	DBaseHelper dbHelper;
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

	public void insert(String tableName, ContentValues values) {
		sqliteDb.insert(tableName, null, values);
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

			// Создание таблицы списка документов.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_NAME
					+ " (_id integer primary key autoincrement, "
					+ FIELD_REF_NAME + " text," + FIELD_NUM_NAME + " text, "
					+ FIELD_DATE_NAME + " text, " + FIELD_STORE_NAME
					+ " text, " + FIELD_COMMENT_NAME + " text, "
					+ FIELD_INDEX_NAME + " text);");

			// Создание таблицы загруженного документа.
			db.execSQL("CREATE TABLE " + TABLE_ITEMS_NAME
					+ " (_id integer primary key autoincrement, "
					+ FIELD_CODE_NAME + " text," + FIELD_NAME_NAME + " text, "
					+ FIELD_NAMEFULL_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

			insertDemoItems(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Будет изменено при обновлении структуры БД.
		}
	}

	/**
	 * Дополняет коллекцию ContentValues индексным полем.
	 * 
	 * @param values
	 * @return String, которой было заполнено индексное поле.
	 */
	public String addItemIndexField(String tableName, ContentValues values) {

		String[] indexFields = null;
		if (tableName == TABLE_ITEMS_NAME) {

			indexFields = new String[] { FIELD_CODE_NAME, FIELD_NAME_NAME,
					FIELD_NAMEFULL_NAME };

		} else if (tableName == TABLE_DOCS_NAME) {

			indexFields = new String[] { FIELD_NUM_NAME, FIELD_DATE_NAME,
					FIELD_STORE_NAME, FIELD_COMMENT_NAME };
		}

		String indexValue = getContentValuesString(values, indexFields)
				.toLowerCase(Locale.getDefault()); // TODO: Применить локаль.

		values.put(FIELD_INDEX_NAME, indexValue);

		return indexValue;
	}

	/**
	 * Возвращает содержимое коллекции ContentValues в виде строки.
	 * 
	 * @param values
	 * @return
	 */
	public String getContentValuesString(ContentValues values, String[] keys) {

		String result = "";

		if (keys == null)
			return result;

		for (int i = 0; i < keys.length; i++) {
			result += values.getAsString(keys[i]);
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

		docsValues.put(FIELD_REF_NAME, "7ba659f6-05f5-4a96-96fe-0c10b72a1608");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005264");
		docsValues.put(FIELD_DATE_NAME, "2014-10-05");
		docsValues.put(FIELD_STORE_NAME, "1 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "Для Гули.");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "b2416c7e-74ed-42e8-bfc3-fc21a5be779b");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005325");
		docsValues.put(FIELD_DATE_NAME, "2014-10-05");
		docsValues.put(FIELD_STORE_NAME, "2 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "Настя можно делать!");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "bbd58850-2d84-4ed1-bdfb-1782f9934b1b");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005368");
		docsValues.put(FIELD_DATE_NAME, "2014-10-06");
		docsValues.put(FIELD_STORE_NAME, "135 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "612a5a2e-c61f-4a23-86d3-ee27d12aa9cf");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005369");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "95 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "Возьмите расписку с киоскера!");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "34b219c0-aa3f-41fc-a67d-4bc05414ad80");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005401");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "151 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "a2d2e074-f0c7-4f4d-b572-5dad3ec2e17e");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005402");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "152 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "cc1a518b-131a-4060-9502-240cdb660101");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005403");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "153 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "f9e9da10-57e5-447a-987d-f00a4b455b76");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005404");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "154 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "68889f55-cb89-4441-aeed-93a950f70bc4");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005405");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "155 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "Срочно!");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "28bad9df-055e-4197-aefc-e044dde77753");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005406");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "156 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "53a68452-3f22-4cb3-8dc4-3287f2322ecf");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005407");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "157 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "85a20773-02ba-4f3e-af47-7849cdc9ee6b");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005408");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "158 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "04a28a10-0329-484b-a30b-4a7983309136");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005409");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "159 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "81b8f28b-b4d5-4a98-a378-00c356a6ae00");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005410");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "160 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "557b36ab-0c3f-42c9-8576-309f35b97451");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005411");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "161 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "cb3ad424-0f2b-49f6-9ee2-f01362fcb1fa");
		docsValues.put(FIELD_NUM_NAME, "ЭКС0005412");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "162 киоск");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		// ///////////////////////////////////////
		// Таблица загруженного документа.
		ContentValues loadedDocsValues = new ContentValues();
		loadedDocsValues.put(FIELD_CODE_NAME, 1);
		loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Натс");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "шоколадный батончик Nuts");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 2);
		loadedDocsValues.put(FIELD_NAME_NAME, "гор.шок.Бабай 75");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"горький шоколад Бабаевский 75%");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 3);
		loadedDocsValues.put(FIELD_NAME_NAME, "сиг.Друг");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "сигареты Друг");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 4);
		loadedDocsValues.put(FIELD_NAME_NAME, "газ. Кр.Зв.");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "газета Красная Звезда");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 5);
		loadedDocsValues.put(FIELD_NAME_NAME, "газ. Мол.Сиб.");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "газета Молодость Сибири");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 6);
		loadedDocsValues.put(FIELD_NAME_NAME, "газ. Комс.Прав.");
		loadedDocsValues
				.put(FIELD_NAMEFULL_NAME, "газета Комсомольская Правда");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 7);
		loadedDocsValues.put(FIELD_NAME_NAME, "жур. НиЖ");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "журнал Наука и жизнь");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "ж");
		loadedDocsValues.put(FIELD_NAME_NAME, "жур. Mens Health");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "журнал Mens Health");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "Б97");
		loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Сникрс 125");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"шоколадный батончик Сникерс, 125 гр.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "Б101");
		loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Баунти 125");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"шоколадный батончик Баунти, 125 гр.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 8);
		loadedDocsValues.put(FIELD_NAME_NAME, "газ.нап.Фанта");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "газированный напиток Фанта");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 9);
		loadedDocsValues.put(FIELD_NAME_NAME, "газ.нап.Тархун");
		loadedDocsValues
				.put(FIELD_NAMEFULL_NAME, "газированный напиток Тархун");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 1001);
		loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Сникрс 200");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"шоколадный батончик Сникерс, 200 гр.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, ".Бат");
		loadedDocsValues.put(FIELD_NAME_NAME, "ж.р.Дирол");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "жевательная резинка Дирол");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 45006);
		loadedDocsValues.put(FIELD_NAME_NAME, "газ.Веч.Нск.");
		loadedDocsValues
				.put(FIELD_NAMEFULL_NAME, "газета Вечерний Новосибирск");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 630005);
		loadedDocsValues.put(FIELD_NAME_NAME, "жур. Поп.Мех.");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "журнал Популярная Механика");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 11);
		loadedDocsValues.put(FIELD_NAME_NAME, "стир.рез.Кохинор");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"стирательная резинка Kohinoor");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 12);
		loadedDocsValues.put(FIELD_NAME_NAME, "шок.бат.Баунти 200");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"шоколадный батончик Баунти, 200 гр.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 14);
		loadedDocsValues.put(FIELD_NAME_NAME, "газ.нап.К-Кола 330 ж/б");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"газированный напиток Кока-кола, 330гр. жест.б.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "_ш07");
		loadedDocsValues.put(FIELD_NAME_NAME, "шок.нап.Nesquick");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "шоколадный напиток Несквик");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "995");
		loadedDocsValues.put(FIELD_NAME_NAME, "ватн.палочки Идеал");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "ватные палочки Идеал");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

	}

	// получить все данные из таблицы DB_TABLE
	public Cursor getAllRows() {
		return sqliteDb.query(TABLE_DOCS_NAME, null, null, null, null, null,
				null);
	}
}