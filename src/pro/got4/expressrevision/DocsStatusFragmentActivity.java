package pro.got4.expressrevision;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Асинхронно выполняет установку статуса документа на сервере.
 * 
 * @author programmer
 * 
 */
public class DocsStatusFragmentActivity extends FragmentActivity implements
		LoaderCallbacks<Boolean> {

	public static final int ID = 300;

	public static final int DOCS_STATUS_LOADER_ID = 301;
	public static final String DOCSLIST_LOADER_TAG = "docsstatusfragmentactivity_tag";

	public static final String FIELD_COMMAND_NAME = "command";
	public static final String FIELD_STATUS_NAME = "status";

	private static final int DEMO_MODE_SLEEP_TIME = 2000;

	public static enum COMMAND {

		/**
		 * Команда на простую установку статуса (без всяких проверок);
		 */
		SET;
	};

	// Поля XML-парсера.
	private static final String DOC_TAG_NAME = "doc";
	private static final String DOC_NUM_TAG_NAME = "num";
	private static final String DOC_DATE_TAG_NAME = "date";
	private static final String DOC_STATUS_TAG_NAME = "status";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.doc_set_status);

		if (!Main.isDemoMode() && !Main.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
			finish();
		}

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this,
					getString(R.string.docsConnectionParametersNotSet),
					Toast.LENGTH_LONG).show();
			finish();
		} else {

			// Проверка комплектности параметов, без которых нет смысла
			// запускать процедуру обмена.

			String connectionString = extras
					.getString(DocsListLoader.CONNECTION_STRING_FIELD_NAME);
			String docNum = extras.getString(DBase.FIELD_DOC_NUM_NAME);
			long docDate = extras.getLong(DBase.FIELD_DOC_DATE_NAME);
			COMMAND command = (COMMAND) extras.get(FIELD_COMMAND_NAME);
			Integer status = extras.getInt(FIELD_STATUS_NAME);

			if (!Main.isDemoMode()
					&& (connectionString == null || connectionString.isEmpty())
					&& (docNum == null || docNum.isEmpty()) && (docDate == 0L)
					&& (command == null || command.toString().isEmpty())
					&& (status == 0)) {

				Toast.makeText(this,
						getString(R.string.docsStatusSettingsNotSet),
						Toast.LENGTH_LONG).show();
				finish();
			}
		}

		getSupportLoaderManager().initLoader(DOCS_STATUS_LOADER_ID, extras,
				this);
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<Boolean> onCreateLoader(int id, Bundle args) {

		Loader<Boolean> ldr = new DocsStatusAsyncTaskLoader(this, args);

		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<Boolean> loader, Boolean data) {

		if (data == false)
			setResult(RESULT_CANCELED);
		else
			setResult(RESULT_OK);

		finish();
	}

	@Override
	public void onLoaderReset(Loader<Boolean> loader) {
		setResult(RESULT_CANCELED);
		finish();
	}

	// /////////////////////////////////////////////////////
	// AsyncTaskLoader<Void>
	// /////////////////////////////////////////////////////
	private static class DocsStatusAsyncTaskLoader extends
			AsyncTaskLoader<Boolean> {

		private Context context;
		private String connectionString;
		private String docNum;
		private long docDate;
		private COMMAND command;
		private int status;

		private SimpleDateFormat dateFormatterTo = new SimpleDateFormat(
				"yyyyMMddHHmmss", Locale.getDefault());

		// Форматтер даты для преобразования из входного формата.
		private SimpleDateFormat dateFormatterFrom = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

		public DocsStatusAsyncTaskLoader(Context context, Bundle args) {

			super(context);

			this.context = context;

			connectionString = args
					.getString(DocsListLoader.CONNECTION_STRING_FIELD_NAME);
			docNum = args.getString(DBase.FIELD_DOC_NUM_NAME);

			// String docDateString = args.getString(DBase.FIELD_DOC_DATE_NAME);
			// docDate = Long.valueOf(docDateString);
			docDate = args.getLong(DBase.FIELD_DOC_DATE_NAME);

			command = (COMMAND) args.get(FIELD_COMMAND_NAME);
			status = args.getInt(FIELD_STATUS_NAME);
		}

		@Override
		public void onStartLoading() {

			forceLoad();

			super.onStartLoading();
		}

		@Override
		public Boolean loadInBackground() {

			if (Main.isDemoMode()) {

				// В демо-режиме статус реально не проверяется.
				try {
					TimeUnit.MILLISECONDS.sleep(DEMO_MODE_SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return true;

			} else {

				// Загрузка с сервера.
				// http://express.nsk.ru:9999/erdocs.php
				// ?deviceid=12345
				// &command=setstatus
				// &status=2
				// &docdate=20150105162307
				// &docnum='ЭКС00000008'
				// Получение идентификатора устройства.

				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String deviceId = tm.getDeviceId();
				String uriString = connectionString + "?deviceid=" + deviceId
						+ "&command=" + getGETCommand(command) + "&status="
						+ status;

				try {

					// Дата в БД имеет вид миллисекунд с начала Юникс-эпохи,
					// и её следует преобразовать к виду "20140101000000".
					uriString = uriString + "&docdate="
							+ dateFormatterTo.format(docDate);

					// В номере документа может содержаться кириллица, поэтому
					// необходимо преобразование.
					uriString = uriString + "&docnum='"
							+ URLEncoder.encode(docNum, HTTP.UTF_8) + "'";

					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

					Document domDoc = dBuilder.parse(uriString);
					domDoc.getDocumentElement().normalize();

					// Получение всех узлов документов.
					NodeList docNodes = domDoc
							.getElementsByTagName(DOC_TAG_NAME);

					int rowsTotal = docNodes.getLength();

					// Должна вернуться хотя-бы одна строка с указанием статуса
					// документа.
					if (rowsTotal == 0)
						return false;

					// Перебор всех документов.
					Boolean result = true;
					for (int docIdx = 0; docIdx < rowsTotal; docIdx++) {

						if (!isStarted() || isAbandoned() || isReset())
							return false;

						Node docNode = docNodes.item(docIdx);
						NamedNodeMap docAttr = docNode.getAttributes();

						// Номер документа.
						Node docNumNode = docAttr
								.getNamedItem(DOC_NUM_TAG_NAME);
						String docNumValue = docNumNode.getTextContent();

						// Дата документа.
						Node docDateTimeNode = docAttr
								.getNamedItem(DOC_DATE_TAG_NAME);
						String docDateTimeValue = docDateTimeNode
								.getTextContent();

						// Преобразование даты документа из строкового вида
						// "2015-01-19 14:50:04" к времени с начала эпохи.
						Date gmt = dateFormatterFrom.parse(docDateTimeValue);
						long docDateTimeEpochValue = gmt.getTime();

						// Статус документа.
						Node docStatusNode = docAttr
								.getNamedItem(DOC_STATUS_TAG_NAME);
						int docStatusValue = Integer.valueOf(docStatusNode
								.getTextContent());

						// Статус считается установленным, если возвращенное
						// значение статуса совпадает со значением, переданным в
						// параметрах.
						if (docNumValue.equals(docNum)
								&& (docDateTimeEpochValue == docDate)) {
							if (docStatusValue != status) {
								result = false;
								break;
							}
						}
					}

					return result;

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

				return false;

			} // if (Main.isDemoMode()) {
		}

		@Override
		public void onStopLoading() {

			cancelLoad();

			super.onStopLoading();

			onReleaseResources();
		}

		@Override
		public void onCanceled(Boolean data) {

			super.onCanceled(data);

			onReleaseResources();
		}

		@Override
		protected void onReset() {

			super.onReset();

			stopLoading();

			onReleaseResources();
		}

		/**
		 * Освобождение ресурсов.
		 */
		private void onReleaseResources() {
			// Пока ресурсов нет.
		}

		/**
		 * Возвращает команду GET-запроса, соответствующую команде, переданной в
		 * загрузчик.
		 * 
		 */
		private String getGETCommand(COMMAND command) {

			switch (command) {
			case SET: {
				return "setstatus";
			}
			default:
				return "";
			}
		}
	}
}
