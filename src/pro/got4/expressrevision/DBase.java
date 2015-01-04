package pro.got4.expressrevision;

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
	public static final String FIELD_STORE_CODE_NAME = "store_code";
	public static final String FIELD_STORE_DESCR_NAME = "store_descr";

	// ������� ������������ ����������� ����������.
	public static final String TABLE_ITEMS_DEMO_NAME = "items_demo";
	public static final String TABLE_ITEMS_NAME = "items";

	// ���� ������ ������������ ����������� ����������.
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

	// ��������� ���� ��� �������������� � ������, �������� ������������.
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss", Locale.getDefault());

	// ��������� ����, ������������ ���������� ��� ������������� ������.
	// �������� ��� ��� ������!!!
	public static final String FIELD_ID_NAME = "_id";

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

			// �������� ������ ������ ����������.
			// �������, �������� ����� ���������� ��� ����-������.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_DEMO_NAME + " ("
					+ FIELD_ID_NAME + " integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " integer, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

			// �������, �������� �������� ����� ����������.
			db.execSQL("CREATE TABLE " + TABLE_DOCS_NAME + " (" + FIELD_ID_NAME
					+ " integer primary key autoincrement, "
					+ FIELD_DOC_NUM_NAME + " text, " + FIELD_DOC_DATE_NAME
					+ " integer, " + FIELD_DOC_COMMENT_NAME + " text, "
					+ FIELD_STORE_CODE_NAME + " text, "
					+ FIELD_STORE_DESCR_NAME + " text, " + FIELD_INDEX_NAME
					+ " text);");

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
					+ FIELD_QUANT_NAME + " real, " + FIELD_INDEX_NAME
					+ " text);");

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
					+ FIELD_QUANT_NAME + " real, " + FIELD_INDEX_NAME
					+ " text);");

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
		if (tableName == TABLE_DOCS_NAME) {

			indexFields = new String[] { FIELD_DOC_NUM_NAME,
					FIELD_DOC_DATE_NAME, FIELD_DOC_COMMENT_NAME,
					FIELD_STORE_DESCR_NAME };

		} else if (tableName == TABLE_ITEMS_NAME) {

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

			if (uniqueOnly) {
				if (result.indexOf(currentValue) == -1) {
					result += currentValue;
				}
			} else {
				result += currentValue;
			}
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

		// SimpleDateFormat formatter = new SimpleDateFormat(
		// "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		// try {
		// Date gmt = formatter.parse("2014-10-05 13:28:15");
		// long millisecondsSinceEpoch = gmt.getTime();
		// String asString = formatter.format(gmt);
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005264");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-05 13:28:15");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "��� ����.");
		docsValues.put(FIELD_STORE_CODE_NAME, "1");
		docsValues.put(FIELD_STORE_DESCR_NAME, "1 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005325");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-05 09:08:22");
		docsValues.put(FIELD_STORE_DESCR_NAME, "2 �����");
		docsValues.put(FIELD_STORE_CODE_NAME, "2");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "����� ����� ������!");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005368");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-06");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "135");
		docsValues.put(FIELD_STORE_DESCR_NAME, "135 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005369");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "�������� �������� � ��������!");
		docsValues.put(FIELD_STORE_CODE_NAME, "95");
		docsValues.put(FIELD_STORE_DESCR_NAME, "95 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005401");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME,
				"����, �� ��������� �������, � ��� ���������.");
		docsValues.put(FIELD_STORE_CODE_NAME, "151");
		docsValues.put(FIELD_STORE_DESCR_NAME, "151 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005402");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "152");
		docsValues.put(FIELD_STORE_DESCR_NAME, "152 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005403");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "153");
		docsValues.put(FIELD_STORE_DESCR_NAME, "153 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005404");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "154");
		docsValues.put(FIELD_STORE_DESCR_NAME, "154 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005405");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "������!");
		docsValues.put(FIELD_STORE_CODE_NAME, "155");
		docsValues.put(FIELD_STORE_DESCR_NAME, "155 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005406");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "156");
		docsValues.put(FIELD_STORE_DESCR_NAME, "156 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005407");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "157");
		docsValues.put(FIELD_STORE_DESCR_NAME, "157 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005408");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "158");
		docsValues.put(FIELD_STORE_DESCR_NAME, "158 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005409");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "159");
		docsValues.put(FIELD_STORE_DESCR_NAME, "159 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005410");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "160");
		docsValues.put(FIELD_STORE_DESCR_NAME, "160 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005411");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "161");
		docsValues.put(FIELD_STORE_DESCR_NAME, "161 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		docsValues.put(FIELD_DOC_NUM_NAME, "���0005412");
		docsValues.put(FIELD_DOC_DATE_NAME, "2014-10-07");
		docsValues.put(FIELD_DOC_COMMENT_NAME, "");
		docsValues.put(FIELD_STORE_CODE_NAME, "162");
		docsValues.put(FIELD_STORE_DESCR_NAME, "162 �����");

		insert(dataBase, TABLE_DOCS_DEMO_NAME, docsValues);

		// ///////////////////////////////////////
		// ������� ����������� ����������.
		// ContentValues loadedDocsValues = new ContentValues();
		// loadedDocsValues.put(FIELD_CODE_NAME, 1);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.����");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "���������� �������� Nuts");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 2);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.����� 75");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "������� ������� ���������� 75%");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 3);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.����");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "�������� ����");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 4);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���. ��.��.");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ������� ������");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 5);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���. ���.���.");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ��������� ������");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 6);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���. ����.����.");
		// loadedDocsValues
		// .put(FIELD_NAMEFULL_NAME, "������ ������������� ������");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 7);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���. ���");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ����� � �����");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "�");
		// loadedDocsValues.put(FIELD_NAME_NAME, "���. Mens Health");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ Mens Health");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "�97");
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 125");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "���������� �������� �������, 125 ��.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "�101");
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 125");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "���������� �������� ������, 125 ��.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 8);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.�����");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "������������ ������� �����");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 9);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������");
		// loadedDocsValues
		// .put(FIELD_NAMEFULL_NAME, "������������ ������� ������");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 1001);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 200");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "���������� �������� �������, 200 ��.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, ".���");
		// loadedDocsValues.put(FIELD_NAME_NAME, "�.�.�����");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "����������� ������� �����");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 45006);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.���.");
		// loadedDocsValues
		// .put(FIELD_NAMEFULL_NAME, "������ �������� �����������");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 630005);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���. ���.���.");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "������ ���������� ��������");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 11);
		// loadedDocsValues.put(FIELD_NAME_NAME, "����.���.�������");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "������������ ������� Kohinoor");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 12);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.������ 200");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "���������� �������� ������, 200 ��.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, 14);
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.�-���� 330 �/�");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "������������ ������� ����-����, 330��. ����.�.");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "_�07");
		// loadedDocsValues.put(FIELD_NAME_NAME, "���.���.Nesquick");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME,
		// "���������� ������� �������");
		//
		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);
		//
		// loadedDocsValues.put(FIELD_CODE_NAME, "995");
		// loadedDocsValues.put(FIELD_NAME_NAME, "����.������� �����");
		// loadedDocsValues.put(FIELD_NAMEFULL_NAME, "������ ������� �����");

		// DBase.this.insert(dataBase, TABLE_ITEMS_DEMO_NAME, loadedDocsValues);

	}

	/**
	 * ���������� ������ �� �� ���������� �������.
	 * 
	 * @return
	 */
	public Cursor getAllRows(String tableName) {
		return sqliteDb.query(tableName, null, null, null, null, null, null);
	}

	/*
	 * ���������� ������ �� ��������������� ���������� �������.
	 */
	public Cursor getFilteredRows(String tableName, String filterString) {

		String filter = FIELD_INDEX_NAME.concat(" LIKE '%")
				.concat(filterString).concat("%'");
		return sqliteDb.query(tableName, null, filter, null, null, null, null);
	}

	/**
	 * ���������� ���������� ����� �������.
	 * 
	 * @param tableName
	 * @return
	 */
	public long getRowsCount(String tableName) {
		return DatabaseUtils.queryNumEntries(sqliteDb, tableName);
	}

	/**
	 * ������� ���������� ��������� �������.
	 * 
	 * @param tableName
	 * @return
	 */
	public int clearTable(String tableName) {
		return sqliteDb.delete(tableName, "1", null);
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

		long rowId = insert(sqliteDb, receiverTableName, itemValues);

		return rowId;
	}
}