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
public class DocsListLoader extends FragmentActivity implements
		LoaderCallbacks<Void>, DialogListener {

	public static final int DOCSLIST_LOADER_ID = 1;
	public static final String DOCSLIST_LOADER_TAG = "docslistloaderfragmentactivity_tag";

	public static final int BUTTON_BACK_ID = 1;

	private ProgressDialogFragment pDialog; // Используется в хэндлере.

	private ProgressHandler progressHandler; // Используется в AsyncTaskLoader.
	private static final int SLEEP_TIME = 500;

	// Во избежание утечек (см. подсказку, которая появляется, если класс
	// хэндлера не делать статическим).
	private static WeakReference<DocsListLoader> WEAK_REF_ACTIVITY;

	// /////////////////////////////////////////////////////
	// Хэндлер.
	// /////////////////////////////////////////////////////

	/**
	 * Хэндлер, принимающий сообщения о статусе текущей загрузки.
	 */
	private static class ProgressHandler extends Handler {

		private WeakReference<DocsListLoader> wrActivity;

		public ProgressHandler(WeakReference<DocsListLoader> wrActivity) {

			Message.show("[hashCode = " + this.hashCode() + "], wrActivity = "
					+ wrActivity);

			this.wrActivity = wrActivity;
		}

		/**
		 * Установка активности, для UI которой хэндлер выполняет действия.
		 * 
		 * @param wrActivity
		 */
		public void setActivity(WeakReference<DocsListLoader> wrActivity) {

			Message.show(this);

			this.wrActivity = wrActivity;
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			// Message.show("[hashCode = " + this.hashCode() + "], what = ["
			// + msg.what + "]");

			// Установка значения прогресса.
			wrActivity.get().pDialog.setProgress(msg.what);
			wrActivity.get().pDialog.setMax(msg.arg1);

			return;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Message.show(this);

		super.onCreate(savedInstanceState);

		WEAK_REF_ACTIVITY = new WeakReference<DocsListLoader>(this);

		// Поиск диалога, ранее созданного для данной активности.
		pDialog = (ProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(DOCSLIST_LOADER_TAG);

		// Если диалог еще не создавался, то его нужно создать.
		if (pDialog == null) {

			pDialog = new ProgressDialogFragment();
			pDialog.setTitle(getString(R.string.loadingDocuments));
			pDialog.setMessage(getString(R.string.pleaseWait));

			pDialog.show(getSupportFragmentManager(), DOCSLIST_LOADER_TAG);
		}

		getSupportLoaderManager().initLoader(DOCSLIST_LOADER_ID, null, this);
	}

	@Override
	public void onResume() {

		Message.show("[hashCode = " + this.hashCode()
				+ "], (before) progressHandler = " + progressHandler);

		// Инициалиация хэндлера прогресс-диалога.
		if (progressHandler == null) {
			progressHandler = new ProgressHandler(WEAK_REF_ACTIVITY);
		} else {
			progressHandler.setActivity(WEAK_REF_ACTIVITY);
		}

		Message.show("[hashCode = " + this.hashCode()
				+ "], (after) progressHandler = " + progressHandler);

		super.onResume();
	}

	@Override
	public void onPause() {

		Message.show(this);

		progressHandler = null;

		super.onPause();
	}

	// /////////////////////////////////////////////////////
	// LoaderCallbacks<Void>
	// /////////////////////////////////////////////////////

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle bndl) {

		Message.show("[hashCode = " + this.hashCode() + "], id = " + id);

		Loader<Void> ldr = new DocsAsyncTaskLoader(this);
		Message.show(ldr);

		return ldr;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void data) {

		Message.show(this);

		setResult(RESULT_OK);
		onCloseDialog(DOCSLIST_LOADER_ID, BUTTON_BACK_ID);

		// Установка даты обновления.
		Main.setLastDocsListFetchingTime(new GregorianCalendar());
	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {

		Message.show(this);

		setResult(RESULT_CANCELED);
		onCloseDialog(DOCSLIST_LOADER_ID, BUTTON_BACK_ID);

		// Установка даты обновления.
		Main.setLastDocsListFetchingTime(new GregorianCalendar());
	}

	// /////////////////////////////////////////////////////
	// AsyncTaskLoader<Void>
	// /////////////////////////////////////////////////////
	private static class DocsAsyncTaskLoader extends AsyncTaskLoader<Void> {

		private Context context;

		private MediaPlayer mp;

		private int rowsTotal;
		private int rowsCounter;

		private DBase dBase;

		public DocsAsyncTaskLoader(Context context) {

			super(context);

			Message.show(this);

			this.context = context;

			rowsTotal = 0;
			rowsCounter = 0;
		}

		@Override
		public void onStartLoading() {

			Message.show(this);

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

			// try {
			//
			// for (int i = 0; i < DocsListLoader.DOCS; i++) {
			//
			// if (!isStarted() || isAbandoned() || isReset())
			// break;
			//
			// TimeUnit.MILLISECONDS.sleep(500);
			//
			// Message.show("[hashCode = " + this.hashCode() + "], i = ["
			// + i + "]");
			//
			// ProgressHandler progressHandler = WEAK_REF_ACTIVITY.get()
			// .getProgressHandler();
			//
			// if (progressHandler != null)
			// progressHandler.sendEmptyMessage(i);
			// }
			//
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }

			if (Main.isDemoMode() || !Main.isDemoMode()) { // TODO Источник
															// загрузки.

				Cursor cursor = dBase.getAllRows(DBase.TABLE_DOCS_DEMO_NAME);

				// Message.show("[hashCode = " + this.hashCode()
				// + ", progressHandler = " + progressHandler);

				rowsTotal = cursor.getCount();
				rowsCounter = 0;
				cursor.moveToFirst();
				if (cursor.isFirst()) {

					Message.show("[hashCode = " + this.hashCode()
							+ "], isStarted() == " + isStarted());

					Message.show("[hashCode = " + this.hashCode()
							+ "], isAbandoned() == " + isAbandoned());

					Message.show("[hashCode = " + this.hashCode()
							+ "], isReset() == " + isReset());

					if (!isStarted() || isAbandoned() || isReset())
						return null;

					++rowsCounter;
					dBase.copyDocRow(DBase.TABLE_DOCS_NAME, cursor);

					setProgress(WEAK_REF_ACTIVITY.get().getProgressHandler(),
							rowsCounter, rowsTotal, 0);

					Message.show("[hashCode = " + this.hashCode()
							+ "], copyItemRow(" + rowsCounter + ")");

					while (cursor.moveToNext()) {

						try {
							TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Message.show("[hashCode = " + this.hashCode()
								+ "], isStarted() == " + isStarted());

						Message.show("[hashCode = " + this.hashCode()
								+ "], isAbandoned() == " + isAbandoned());

						Message.show("[hashCode = " + this.hashCode()
								+ "], isReset() == " + isReset());

						if (!isStarted() || isAbandoned() || isReset())
							break;

						++rowsCounter;
						dBase.copyDocRow(DBase.TABLE_DOCS_NAME, cursor);
						setProgress(WEAK_REF_ACTIVITY.get()
								.getProgressHandler(), rowsCounter, rowsTotal,
								0);

						Message.show("[hashCode = " + this.hashCode()
								+ "], copyItemRow(" + rowsCounter + ")");

					}
				}

			} else {
				// Загрузка с сервера.
			}

			return null;
		}

		@Override
		public void onStopLoading() {

			Message.show(this);

			cancelLoad();

			super.onStopLoading();

			onReleaseResources();
		}

		@Override
		public void onCanceled(Void data) {

			Message.show(this);

			super.onCanceled(data);

			onReleaseResources();
		}

		@Override
		protected void onReset() {

			Message.show(this);

			super.onReset();

			stopLoading();

			onReleaseResources();
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

			Message.show("[hashCode = " + this.hashCode()
					+ ", progressHandler = " + progressHandler);

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

		onCloseDialog(DOCSLIST_LOADER_ID, BUTTON_BACK_ID);
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

		getSupportLoaderManager().getLoader(DOCSLIST_LOADER_ID).abandon();

		finish();
	}
}
