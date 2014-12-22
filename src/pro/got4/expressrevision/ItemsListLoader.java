package pro.got4.expressrevision;

import java.lang.ref.WeakReference;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import pro.got4.expressrevision.ProgressDialogFragment.DialogListener;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

/**
 * Асинхронный загрузчик, выводящий прогресс-диалог.
 * 
 * @author programmer
 * 
 */
public class ItemsListLoader extends FragmentActivity implements
		LoaderCallbacks<Void>, DialogListener {

	public static final int ITEMSLIST_LOADER_ID = 1;
	public static final String ITEMSLIST_LOADER_TAG = "itemslistloaderfragmentactivity_tag";

	private static final String FIELD_ROWSCOUNTER_NAME = "rowsCounter";
	private static final String FIELD_ROWSTOTAL_NAME = "rowsTotal";

	public static final int BUTTON_BACK_ID = 1;

	private ProgressDialogFragment pDialog; // Используется в хэндлере.

	private ProgressHandler progressHandler; // Используется в AsyncTaskLoader.

	private static final int SLEEP_TIME = 500;

	private int rowsCounter;
	private int rowsTotal;

	// Во избежание утечек (см. подсказку, которая появляется, если класс
	// хэндлера не делать статическим).
	private static WeakReference<ItemsListLoader> WEAK_REF_ACTIVITY;

	// /////////////////////////////////////////////////////
	// Хэндлер.
	// /////////////////////////////////////////////////////

	/**
	 * Хэндлер, принимающий сообщения о статусе текущей загрузки.
	 */
	private static class ProgressHandler extends Handler {

		private WeakReference<ItemsListLoader> wrActivity;

		public ProgressHandler(WeakReference<ItemsListLoader> wrActivity) {

			// Message.show(this);

			this.wrActivity = wrActivity;
		}

		/**
		 * Установка активности, для UI которой хэндлер выполняет действия.
		 * 
		 * @param wrActivity
		 */
		public void setActivity(WeakReference<ItemsListLoader> wrActivity) {

			Message.show(this);

			this.wrActivity = wrActivity;
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			Message.show("[hashCode = " + this.hashCode() + "], what = ["
					+ msg.what + "]");

			// Установка значения прогресса.
			wrActivity.get().pDialog.setProgress(msg.what);
			wrActivity.get().pDialog.setMax(msg.arg1);

			return;
		}
	};

	public ItemsListLoader() {

		rowsCounter = 0;
		rowsTotal = 0;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Message.show(this);

		super.onCreate(savedInstanceState);

		WEAK_REF_ACTIVITY = new WeakReference<ItemsListLoader>(this);

		// Поиск диалога, ранее созданного для данной активности.
		pDialog = (ProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(ITEMSLIST_LOADER_TAG);

		// Если диалог еще не создавался, то его нужно создать.
		if (pDialog == null) {

			pDialog = new ProgressDialogFragment();
			pDialog.setTitle(getString(R.string.loadingItems));
			pDialog.setMessage(getString(R.string.pleaseWait));

			pDialog.show(getSupportFragmentManager(), ITEMSLIST_LOADER_TAG);
		}

		if (savedInstanceState != null) {

			rowsCounter = savedInstanceState.getInt(FIELD_ROWSCOUNTER_NAME, 0);
			rowsTotal = savedInstanceState.getInt(FIELD_ROWSTOTAL_NAME, 0);
		}

		getSupportLoaderManager().initLoader(ITEMSLIST_LOADER_ID, null, this);
	}

	@Override
	public void onResume() {

		Message.show(this);

		// Инициалиация хэндлера прогресс-диалога.
		if (progressHandler == null) {
			progressHandler = new ProgressHandler(WEAK_REF_ACTIVITY);
		} else {
			progressHandler.setActivity(WEAK_REF_ACTIVITY);
		}

		super.onResume();
	}

	@Override
	public void onPause() {

		super.onPause();

		Message.show(this);

		progressHandler = null;

		// Если загружены не все строки, то результат -1.
		// Если всё, то 1.
		if (rowsCounter == rowsTotal) {
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		outState.putInt(FIELD_ROWSTOTAL_NAME, rowsTotal);
		outState.putInt(FIELD_ROWSCOUNTER_NAME, rowsCounter);
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle bndl) {

		Message.show("[hashCode = " + this.hashCode() + "], id = " + id);

		Loader<Void> ldr = new ItemsAsyncTaskLoader(this);
		Message.show(ldr);

		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void data) {

		Message.show(this, 0);

		setResult(RESULT_OK);
		onCloseDialog(ITEMSLIST_LOADER_ID, BUTTON_BACK_ID);

		// Установка даты обновления.
		Main.setLastItemsListFetchingTime(new GregorianCalendar());
	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {

		Message.show(this, 0);

		setResult(RESULT_CANCELED);
		onCloseDialog(ITEMSLIST_LOADER_ID, BUTTON_BACK_ID);

		// Установка даты обновления.
		Main.setLastItemsListFetchingTime(new GregorianCalendar());
	}

	// /////////////////////////////////////////////////////
	// AsyncTaskLoader<Void>
	// /////////////////////////////////////////////////////
	private static class ItemsAsyncTaskLoader extends AsyncTaskLoader<Void> {

		private Context context;

		private MediaPlayer mp;

		// private int rowsTotal;
		// private int rowsCounter;

		private DBase dBase;

		public ItemsAsyncTaskLoader(Context context) {

			super(context);

			Message.show(this);

			this.context = context;
		}

		@Override
		public void onStartLoading() {

			Message.show(this, 0);

			dBase = new DBase(context);
			dBase.open();

			mp = MediaPlayer.create(context, R.raw.zx);
			mp.seekTo(18000);
			mp.setLooping(true);
			mp.start();

			forceLoad();

			super.onStartLoading();
		}

		// @Override
		// public Void onLoadInBackground() {
		//
		// // Message.show(this);
		//
		// return super.onLoadInBackground();
		// }

		@Override
		public Void loadInBackground() {

			Message.show(this);

			if (Main.isDemoMode() || !Main.isDemoMode()) {

				Cursor cursor = dBase.getAllRows(DBase.TABLE_ITEMS_DEMO_NAME);

				// Приходится хранить значения счетчиков в родительской
				// активности, т.к. их нужно будет проверять в событии
				// onPause(), чтобы сделать вывод о том, полностью ли загружены
				// данные.
				WEAK_REF_ACTIVITY.get().setRowsTotal(cursor.getCount());
				WEAK_REF_ACTIVITY.get().setRowsCounter(0);
				cursor.moveToFirst();
				if (cursor.isFirst()) {

					if (!isStarted() || isAbandoned() || isReset())
						return null;

					WEAK_REF_ACTIVITY.get().incrementRowsCounter(1);
					dBase.copyItemRow(DBase.TABLE_ITEMS_NAME, cursor);

					Message.show("[hashCode = " + this.hashCode()
							+ "], copyItemRow("
							+ WEAK_REF_ACTIVITY.get().getRowsCounter() + ")");

					setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(),
							WEAK_REF_ACTIVITY.get().getRowsCounter(),
							WEAK_REF_ACTIVITY.get().getRowsTotal(), 0);

					while (cursor.moveToNext()) {

						try {
							TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (!isStarted() || isAbandoned() || isReset())
							break;

						WEAK_REF_ACTIVITY.get().incrementRowsCounter(1);
						dBase.copyItemRow(DBase.TABLE_ITEMS_NAME, cursor);

						Message.show("[hashCode = " + this.hashCode()
								+ "], copyItemRow("
								+ WEAK_REF_ACTIVITY.get().getRowsCounter()
								+ ")");

						setProgress(WEAK_REF_ACTIVITY.get()
								.getProgressHandler(), WEAK_REF_ACTIVITY.get()
								.getRowsCounter(), WEAK_REF_ACTIVITY.get()
								.getRowsTotal(), 0);
					}
				}

			} else {
				// Загрузка с сервера.
			} // if (Main.isDemoMode() || !Main.isDemoMode()) {

			return null;
		}

		@Override
		public void onStopLoading() {

			Message.show(this, 0);

			cancelLoad();

			super.onStopLoading();

			onReleaseResources();
		}

		@Override
		public void onCanceled(Void data) {

			Message.show(this, 0);

			super.onCanceled(data);

			onReleaseResources();
		}

		@Override
		protected void onReset() {

			Message.show(this, 0);

			super.onReset();

			stopLoading();

			onReleaseResources();
		}

		@Override
		public void onAbandon() {

			Message.show(this, 0);

			super.onAbandon();
		}

		/**
		 * Освобождение ресурсов.
		 */
		private void onReleaseResources() {
			if (mp != null)
				mp.release();
		}

		/**
		 * Установка состояния индикатора.
		 */
		private void setProgress(ProgressHandler progressHandler, int what,
				int arg1, int arg2) {

			// Установка состояния индикатора.
			if (progressHandler != null) {

				android.os.Message msg = new android.os.Message();
				msg.what = what;
				msg.arg1 = arg1;
				msg.arg2 = arg2;

				progressHandler.sendMessage(msg);
			}
		}

		// @Override
		// public void deliverResult(Void data) {
		//
		// Message.show(this);
		//
		// super.deliverResult(data);
		// }

		// @Override
		// public void onAbandon() {
		//
		// Message.show(this);
		//
		// super.onAbandon();
		// }

		// @Override
		// public void onContentChanged() {
		//
		// Message.show(this);
		//
		// super.onContentChanged();
		// }

		// @Override
		// public void onForceLoad() {
		//
		// Message.show(this);
		//
		// super.onForceLoad();
		// }
	}

	@Override
	public void onBackPressed() {

		Message.show(this);

		super.onBackPressed();

		onCloseDialog(ITEMSLIST_LOADER_ID, BUTTON_BACK_ID);
	}

	/**
	 * Возвращает ссылку на текущий хендлер.
	 * 
	 * @return
	 */
	public ProgressHandler getProgressHandler() {

		// Message.show(this);

		return progressHandler;
	}

	// Слушатель прогресс-диалога.
	public void onCloseDialog(int dialogId, int buttonId) {

		Message.show("[hashCode = " + this.hashCode() + "dialogId = "
				+ dialogId + ", buttonId = " + buttonId);

		getSupportLoaderManager().getLoader(ITEMSLIST_LOADER_ID).abandon();

		finish();
	}

	/**
	 * @param rowsCounter
	 *            the rowsCounter to set
	 */
	public void setRowsCounter(int rowsCounter) {
		this.rowsCounter = rowsCounter;
	}

	public int getRowsCounter() {
		return rowsCounter;
	}

	/**
	 * @param rowsCounter
	 *            the rowsCounter to set
	 */
	public void incrementRowsCounter(int increment) {
		this.rowsCounter += increment;
	}

	/**
	 * @param rowsTotal
	 *            the rowsTotal to set
	 */
	public void setRowsTotal(int rowsTotal) {
		this.rowsTotal = rowsTotal;
	}

	public int getRowsTotal() {
		return rowsTotal;
	}

}
