package pro.got4.expressrevision.dialogs;

import pro.got4.expressrevision.Message;
import pro.got4.expressrevision.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomDialogFragment extends DialogFragment {

	public static String FIELD_TITLE_NAME = "title";
	public static String FIELD_MESSAGE_NAME = "message";
	public static String FIELD_STYLE_NAME = "style";
	public static String FIELD_THEME_NAME = "theme";
	public static String FIELD_DIALOGTAG_NAME = "dialogTag";
	public static String FIELD_DIALOGID_NAME = "dialogId";

	private static String DIALOG_YESNO_TAG = "dialog_yesno_tag";
	private static String DIALOG_PROGRESS_TAG = "dialog_progress_tag";

	public static int BUTTON_NO = 0;
	public static int BUTTON_YES = 1;
	public static int BUTTON_CANCEL = 2;

	private String title;
	private String message;
	private int style;
	private int theme;
	private static String dialogTag;
	private int dialogId;

	private int progress;
	private int max;

	private OnCloseCustomDialogListener listener;

	private ProgressBar progressBar;
	private TextView textViewTitle;
	private TextView textViewMessage;

	private Button buttonYes;
	private Button buttonNo;
	private Button buttonCancel;

	public static interface OnCloseCustomDialogListener {
		public void onCloseCustomDialog(int dialogId, int buttonId);
	}

	@Override
	public void onAttach(Activity activity) {

		Message.show(null);

		super.onAttach(activity);

		try {
			listener = ((OnCloseCustomDialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement interface OnCloseCustomDialogListener!");
		}
	}

	@Override
	public void onCreate(Bundle bundle) {

		Message.show(null);

		super.onCreate(bundle);

		setTitle(getArguments().getString(FIELD_TITLE_NAME));
		setMessage(getArguments().getString(FIELD_MESSAGE_NAME));
		setStyle(getArguments().getInt(FIELD_STYLE_NAME));
		setTheme(getArguments().getInt(FIELD_THEME_NAME));
		dialogTag = getArguments().getString(FIELD_DIALOGTAG_NAME);
		dialogId = getArguments().getInt(FIELD_DIALOGID_NAME);

		setStyle(getStyle(), getTheme());

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

		Message.show(null);

		View v = null;

		if (dialogTag == DIALOG_YESNO_TAG) {

			v = inflater.inflate(R.layout.dialog_yes_no, container, false);
			textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
			textViewMessage = (TextView) v.findViewById(R.id.textViewMessage);
			buttonYes = (Button) v.findViewById(R.id.buttonYes);
			buttonNo = (Button) v.findViewById(R.id.buttonNo);

			textViewTitle.setText(getTitle());
			textViewMessage.setText(getMessage());

			buttonYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					listener.onCloseCustomDialog(dialogId, BUTTON_YES);
					closeCustomDialog();
				}
			});

			buttonNo.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					listener.onCloseCustomDialog(dialogId, BUTTON_NO);
					closeCustomDialog();
				}
			});

		} else if (dialogTag == DIALOG_PROGRESS_TAG) {

			v = inflater.inflate(R.layout.dialog_progress, container, false);
			textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
			textViewMessage = (TextView) v.findViewById(R.id.textViewMessage);

			progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

			buttonCancel = (Button) v.findViewById(R.id.buttonCancel);

			textViewTitle.setText(getTitle());
			textViewMessage.setText(getMessage());

			buttonCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					listener.onCloseCustomDialog(dialogId, BUTTON_NO);
					closeCustomDialog();
				}
			});
		}

		return v;
	}

	private void closeCustomDialog() {

		// Закрытие самого себя.
		FragmentManager fm = getActivity().getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment currentFragment = fm.findFragmentByTag(dialogTag);
		if (currentFragment != null) {
			ft.remove(currentFragment);
		}
		ft.commit();
	}

	/**
	 * Создает диалог с кнопками Да/Нет.
	 */
	public static CustomDialogFragment showDialog_YesNo(String title,
			String message, int dialogId, FragmentActivity fragmentActivity) {

		Message.show(null);

		CustomDialogFragment dialog = new CustomDialogFragment();

		Bundle args = new Bundle();
		args.putString(FIELD_TITLE_NAME, title);
		args.putString(FIELD_MESSAGE_NAME, message);

		// Стили:
		// STYLE_NO_FRAME - фрейм полупрозрачный, воспринимает нажатия;
		// STYLE_NO_INPUT - фрейм полупрозрачный, нажатия воспринимает фоновый
		// фрейм;
		// STYLE_NO_TITLE - фрейм не имеет заголовка, воспринимает нажатия;
		// STYLE_NORMAL - имеет заголовок, воспринимает нажатия.
		args.putInt(FIELD_STYLE_NAME, DialogFragment.STYLE_NO_TITLE);
		args.putInt(FIELD_THEME_NAME, 0);
		args.putString(FIELD_DIALOGTAG_NAME, DIALOG_YESNO_TAG);

		args.putInt(FIELD_DIALOGID_NAME, dialogId);

		dialog.setArguments(args);

		FragmentManager fm = fragmentActivity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		dialog.show(ft, DIALOG_YESNO_TAG); // Коммит производится здесь!

		return dialog;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the style
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(int style) {
		this.style = style;
	}

	/**
	 * @return the theme
	 */
	@Override
	public int getTheme() {
		return super.getTheme();
	}

	/**
	 * @param theme
	 *            the theme to set
	 */
	public void setTheme(int theme) {
		this.theme = theme;
	}

	/**
	 * @return the currentValue
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * @param progress
	 *            the currentValue to set
	 */
	public void setProgress(int progress) {

		this.progress = progress;

		if (progressBar != null)
			progressBar.setProgress(progress);
	}

	/**
	 * @return the maxValue
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max
	 *            the maxValue to set
	 */
	public void setMax(int max) {

		Message.show(null);

		this.max = max;

		if (progressBar != null)
			progressBar.setMax(max);
	}
}
