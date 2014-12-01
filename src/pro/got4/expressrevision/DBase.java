package pro.got4.expressrevision;

import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBase {

	public static final String DB_NAME = "ExpressRevision";

	// ������� ����������.
	public static final String TABLE_DOCS_NAME = "documents";
	public static final String FIELD_REF_NAME = "ref";
	public static final String FIELD_NUM_NAME = "number";
	public static final String FIELD_DATE_NAME = "date";
	public static final String FIELD_STORE_NAME = "store";
	public static final String FIELD_COMMENT_NAME = "comment";

	// ������� ������������ ���������.
	public static final String TABLE_ITEMS_NAME = "items";
	public static final String FIELD_CODE_NAME = "code";
	public static final String FIELD_NAME_NAME = "name";
	public static final String FIELD_NAMEFULL_NAME = "namefull";

	// ��������� ����, ������������ ��� ������ � ��������.
	public static final String FIELD_INDEX_NAME = "idx";

	private final static int DBVersion = 1;

	Context context;
	DBaseHelper dbHelper;
	private SQLiteDatabase db;

	public DBase(Context context) {
		this.context = context;
	}

	public void open() {
		dbHelper = new DBaseHelper();
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		db.close();
	}

	public void insert(String tableName, ContentValues values) {
		db.insert(tableName, null, values);
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

	public SQLiteDatabase getSQLiteDatabase() {
		return db;
	}

	private class DBaseHelper extends SQLiteOpenHelper {

		public DBaseHelper() {
			super(DBase.this.context, DB_NAME, null, DBVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			// �������� ������� ������ ����������.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_NAME
					+ " (_id integer primary key autoincrement, "
					+ FIELD_REF_NAME + " text," + FIELD_NUM_NAME + " text, "
					+ FIELD_DATE_NAME + " text, " + FIELD_STORE_NAME
					+ " text, " + FIELD_COMMENT_NAME + " text, "
					+ FIELD_INDEX_NAME + " text);");

			// �������� ������� ������������ ���������.
			db.execSQL("CREATE TABLE " + TABLE_ITEMS_NAME
					+ " (_id integer primary key autoincrement, "
					+ FIELD_CODE_NAME + " text," + FIELD_NAME_NAME + " text, "
					+ FIELD_NAMEFULL_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

			insertDemoItems(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// ����� �������� ��� ���������� ��������� ��.
		}
	}

	/**
	 * ��������� ��������� ContentValues ��������� �����.
	 * 
	 * @param values
	 * @return String, ������� ���� ��������� ��������� ����.
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
				.toLowerCase(Locale.getDefault()); // TODO: ��������� ������.

		values.put(FIELD_INDEX_NAME, indexValue);

		return indexValue;
	}

	/**
	 * ���������� ���������� ��������� ContentValues � ���� ������.
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
	 * ��������� �� ���������������� ������� ������������.
	 */
	public void insertDemoItems(SQLiteDatabase dataBase) {

		// ///////////////////////////////////////
		// ������� ������ ����������.
		ContentValues docsValues = new ContentValues();
		docsValues.put(FIELD_REF_NAME, "7ba659f6-05f5-4a96-96fe-0c10b72a1608");
		docsValues.put(FIELD_NUM_NAME, "���0005264");
		docsValues.put(FIELD_DATE_NAME, "2014-10-05");
		docsValues.put(FIELD_STORE_NAME, "1 �����");
		docsValues.put(FIELD_COMMENT_NAME, "��� ����.");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "b2416c7e-74ed-42e8-bfc3-fc21a5be779b");
		docsValues.put(FIELD_NUM_NAME, "���0005325");
		docsValues.put(FIELD_DATE_NAME, "2014-10-05");
		docsValues.put(FIELD_STORE_NAME, "2 �����");
		docsValues.put(FIELD_COMMENT_NAME, "����� ����� ������!");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "bbd58850-2d84-4ed1-bdfb-1782f9934b1b");
		docsValues.put(FIELD_NUM_NAME, "���0005368");
		docsValues.put(FIELD_DATE_NAME, "2014-10-06");
		docsValues.put(FIELD_STORE_NAME, "135 �����");
		docsValues.put(FIELD_COMMENT_NAME, "");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		docsValues.put(FIELD_REF_NAME, "612a5a2e-c61f-4a23-86d3-ee27d12aa9cf");
		docsValues.put(FIELD_NUM_NAME, "���0005369");
		docsValues.put(FIELD_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_STORE_NAME, "95 �����");
		docsValues.put(FIELD_COMMENT_NAME, "�������� �������� � ��������!");

		insert(dataBase, TABLE_DOCS_NAME, docsValues);

		// ///////////////////////////////////////
		// ������� ������������ ���������.
		ContentValues loadedDocsValues = new ContentValues();
		loadedDocsValues.put(FIELD_CODE_NAME, 1);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.����");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "���������� �������� Nuts");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 2);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.����� 75");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"������� ������� ���������� 75%");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 3);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.����");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "�������� ����");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 4);
		loadedDocsValues.put(FIELD_NAME_NAME, "���. ��.��.");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ������� ������");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 5);
		loadedDocsValues.put(FIELD_NAME_NAME, "���. ���.���.");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ��������� ������");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 6);
		loadedDocsValues.put(FIELD_NAME_NAME, "���. ����.����.");
		loadedDocsValues
				.put(FIELD_NAMEFULL_NAME, "������ ������������� ������");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 7);
		loadedDocsValues.put(FIELD_NAME_NAME, "���. ���");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ����� � �����");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "�");
		loadedDocsValues.put(FIELD_NAME_NAME, "���. Mens Health");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ Mens Health");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "�97");
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 125");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"���������� �������� �������, 125 ��.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "�101");
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 125");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"���������� �������� ������, 125 ��.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 8);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.�����");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������������ ������� �����");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 9);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������");
		loadedDocsValues
				.put(FIELD_NAMEFULL_NAME, "������������ ������� ������");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 1001);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 200");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"���������� �������� �������, 200 ��.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, ".���");
		loadedDocsValues.put(FIELD_NAME_NAME, "�.�.�����");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "����������� ������� �����");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 45006);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.���.");
		loadedDocsValues
				.put(FIELD_NAMEFULL_NAME, "������ �������� �����������");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 630005);
		loadedDocsValues.put(FIELD_NAME_NAME, "���. ���.���.");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ���������� ��������");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 11);
		loadedDocsValues.put(FIELD_NAME_NAME, "����.���.�������");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"������������ ������� Kohinoor");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 12);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 200");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"���������� �������� ������, 200 ��.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, 14);
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.�-���� 330 �/�");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME,
				"������������ ������� ����-����, 330��. ����.�.");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "_�07");
		loadedDocsValues.put(FIELD_NAME_NAME, "���.���.Nesquick");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "���������� ������� �������");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

		loadedDocsValues.put(FIELD_CODE_NAME, "995");
		loadedDocsValues.put(FIELD_NAME_NAME, "����.������� �����");
		loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ������� �����");

		DBase.this.insert(dataBase, TABLE_ITEMS_NAME, loadedDocsValues);

	}

	// �������� ��� ������ �� ������� DB_TABLE
	public Cursor getAllDocs() {
		return db.query(TABLE_DOCS_NAME, null, null, null, null, null, null);
	}
}