package pro.got4.expressrevision.dialogs;

import pro.got4.expressrevision.Message;
import pro.got4.expressrevision.R;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressDialogFragment extends DialogFragment {

	public static final int DIALOG_PROGRESS_ID = 0;
	public static final String DIALOG_PROGRESS_TAG = "progress_dialog";

	public static final int BUTTON_CANCEL = 0;

	private DialogListener listener;

	private String title;
	private String message;

	private int progress;
	private int max;
	private boolean indeterminate;

	private int style;
	private int theme;

	private TextView textViewTitle;
	private TextView textViewMessage;
	private ProgressBar progressBar;
	private Button buttonCancel;

	/**
	 * Интерфейс, используемый диалогом, чтобы оповещать активность о своих
	 * действиях.
	 * 
	 * @author programmer
	 * 
	 */
	public static interface DialogListener {
		public void onCloseDialog(int dialogId, int buttonId);
	}

	@Override
	public void onAttach(Activity activity) {

		Message.show(this);

		super.onAttach(activity);

		setRetainInstance(true);
		setCancelable(true);

		try {
			listener = ((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement interface DialogListener!");
		}

	}

	@Override
	public void onCreate(Bundle bundle) {

		// Message.show(this);

		super.onCreate(bundle);
	}

	// Здесь можно создать диалог с помощью билдера и не использовать потом
	// onCreateView.
	// @Override
	// public Dialog onCreateDialog(Bundle savedInstanceState) {
	//
	// return super.onCreateDialog(savedInstanceState);
	// }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Message.show(this);

		View v = inflater.inflate(R.layout.dialog_progress, container, false);

		textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
		textViewMessage = (TextView) v.findViewById(R.id.textViewMessage);
		buttonCancel = (Button) v.findViewById(R.id.buttonCancel);
		progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

		textViewTitle.setText(title);
		textViewMessage.setText(message);
		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Message.show(this);

				listener.onCloseDialog(DIALOG_PROGRESS_ID, BUTTON_CANCEL);
			}
		});

		setStyle(style, theme);

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);

		setIndeterminate(1);
	}

	@Override
	public void onDestroyView() {

		// Message.show(this);

		// Следует реализовывать таким образом по причине бага:
		// https://code.google.com/p/android/issues/detail?id=17423
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);

		super.onDestroyView();
	}

	@Override
	public void onCancel(DialogInterface dialog) {

		Message.show(this);

		super.onCancel(dialog);

		listener.onCloseDialog(DIALOG_PROGRESS_ID, BUTTON_CANCEL);
	}

	@Override
	public void onDetach() {

		Message.show(this);

		super.onDetach();

		listener = null;
	}

	// Из-за бага нельзя будет открывать диалог в событии onActivityResult().
	// Решение состоит в перегрузке метода show().
	// https://code.google.com/p/android/issues/detail?id=23761
	// @Override
	// public int show(FragmentTransaction transaction, String tag) {
	// return show(transaction, tag, false);
	// }

	// public int show(FragmentTransaction transaction, String tag,
	// boolean allowStateLoss) {
	//
	// transaction.add(this, tag);
	// super.mRemoved = false;
	// mBackStackId = allowStateLoss ? transaction.commitAllowingStateLoss()
	// : transaction.commit();
	//
	// return mBackStackId;
	// }

	// /////////////////////////////////
	// Сервисные.
	/**
	 * Установка заголовка.
	 * 
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {

		// Message.show(this);

		// Метод м.б. вызван, когда экземпляр диалога уже создан, но самого
		// диалога еще нет (например, в событии onCreate родительской
		// активности), тогда производится только установка значения поля, а
		// текст для вью не устанавливается - он будет установлен позже, самим
		// диалогом в событии onCreateView().
		// Если же метод вызван, когда форма диалога уже сформирована и
		// существует, то можно сразу установить значение и для поля и для
		// самого вью.

		this.title = title;

		if (textViewTitle != null)
			textViewTitle.setText(title);
	}

	/**
	 * Установка сообщения.
	 * 
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {

		// Message.show(this);

		// Метод м.б. вызван, когда экземпляр диалога уже создан, но самого
		// диалога еще нет (например, в событии onCreate родительской
		// активности), тогда производится только установка значения поля, а
		// текст для вью не устанавливается - он будет установлен позже, самим
		// диалогом в событии onCreateView().
		// Если же метод вызван, когда форма диалога уже сформирована и
		// существует, то можно сразу установить значение и для поля и для
		// самого вью.

		this.message = message;

		if (textViewMessage != null)
			textViewMessage.setText(message);
	}

	/**
	 * Установка максимального значения индикатора.
	 * 
	 * @param max
	 *            the maxValue to set
	 */
	public void setMax(int max) {

		Message.show(this);

		this.max = max;

		if (progressBar != null)
			progressBar.setMax(max);
	}

	/**
	 * Установка текущего значения индикатора.
	 * 
	 * @param progress
	 *            the currentValue to set
	 */
	public void setProgress(int progress) {

		this.progress = progress;

		if (progressBar != null)
			progressBar.setProgress(progress);
	}

	/**
	 * Установка текущего значения индикатора.
	 * 
	 * @param progress
	 *            the currentValue to set
	 */
	public void setIndeterminate(int value) {

		if (value == 0)
			this.indeterminate = false;
		else
			this.indeterminate = true;

		if (progressBar != null) {
			progressBar.setIndeterminate(this.indeterminate);
		}
	}

	/**
	 * Установка стиля. Вызов имеет смысл только из события onCreate()
	 * родительской активности, когда onCreateView() еще не вызван.
	 * 
	 * @param style
	 *            the style to set
	 */
	public void setStyle(int style) {

		// Message.show(this);

		this.style = style;
	}

	/**
	 * Установка темы оформления. Вызов имеет смысл только из события onCreate()
	 * родительской активности, когда onCreateView() еще не вызван.
	 * 
	 * @param theme
	 *            the theme to set
	 */
	public void setTheme(int theme) {

		// Message.show(this);

		this.theme = theme;
	}
}
