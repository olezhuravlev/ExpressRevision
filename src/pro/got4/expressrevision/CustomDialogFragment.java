package pro.got4.expressrevision;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.TextView;

public class CustomDialogFragment extends DialogFragment {

	public static String FIELD_TITLE_NAME = "title";
	public static String FIELD_MESSAGE_NAME = "message";
	public static String FIELD_STYLE_NAME = "style";
	public static String FIELD_THEME_NAME = "theme";
	public static String FIELD_DIALOGTAG_NAME = "dialogTag";

	private static String DIALOG_YESNO_TAG = "dialog_yesno_tag";

	public static int BUTTON_NO = 0;
	public static int BUTTON_YES = 1;

	private String title;
	private String message;
	private int style;
	private int theme;
	private String dialogTag;

	private OnCloseCustomDialogListener listener;

	public static interface OnCloseCustomDialogListener {
		public void onCloseCustomDialog(int id);
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		try {
			listener = ((OnCloseCustomDialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnCloseDialogListener");
		}
	}

	@Override
	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);

		title = getArguments().getString(FIELD_TITLE_NAME);
		message = getArguments().getString(FIELD_MESSAGE_NAME);

		style = getArguments().getInt(FIELD_STYLE_NAME);
		theme = getArguments().getInt(FIELD_THEME_NAME);

		dialogTag = getArguments().getString(FIELD_DIALOGTAG_NAME);

		setStyle(style, theme);

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Здесь можно создать диалог с помощью билдера и не использовать потом
		// onCreateView.
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.dialog_yes_no, container, false);
		TextView textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
		TextView textViewMessage = (TextView) v
				.findViewById(R.id.textViewMessage);
		Button buttonYes = (Button) v.findViewById(R.id.buttonYes);
		Button buttonNo = (Button) v.findViewById(R.id.buttonNo);

		textViewTitle.setText(title);
		textViewMessage.setText(message);

		buttonYes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listener.onCloseCustomDialog(BUTTON_YES);
				closeCustomDialog();
			}
		});

		buttonNo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listener.onCloseCustomDialog(BUTTON_NO);
				closeCustomDialog();
			}
		});

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
			String message, FragmentActivity fragmentActivity) {

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

		dialog.setArguments(args);

		FragmentManager fm = fragmentActivity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		// Fragment prev = fm.findFragmentByTag(DIALOG_YESNO_ID);
		// if (prev != null) {
		// ft.remove(prev);
		// }
		// ft.addToBackStack(null);

		dialog.show(ft, DIALOG_YESNO_TAG); // Коммит производится здесь!

		return dialog;
	}
}
