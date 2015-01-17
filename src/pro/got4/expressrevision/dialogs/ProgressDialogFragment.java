package pro.got4.expressrevision.dialogs;

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

	public static final String FIELD_TITLE_NAME = "title";
	public static final String FIELD_MESSAGE_NAME = "message";
	public static final String FIELD_INCREMENT_MODE_NAME = "incrementMode";
	public static final String FIELD_INDETERMINATE_NAME = "indeterminate";
	public static final String FIELD_MAX_NAME = "max";
	public static final String FIELD_PROGRESS_NAME = "progress";

	public static final int BUTTON_CANCEL = 0;

	private DialogListener listener;

	private String title;
	private String message;

	private boolean incrementMode;

	private boolean indeterminate;
	private int max;
	private int progress;

	private int style;
	private int theme;

	private TextView textViewTitle;
	private TextView textViewMessage;
	private ProgressBar progressBar;
	private Button buttonCancel;

	/**
	 * ���������, ������������ ��������, ����� ��������� ���������� � �����
	 * ���������.
	 * 
	 * @author programmer
	 * 
	 */
	public static interface DialogListener {
		public void onCloseDialog(int dialogId, int buttonId);
	}

	// ����� ����� ������� ������ � ������� ������� � �� ������������ �����
	// onCreateView.
	// @Override
	// public Dialog onCreateDialog(Bundle savedInstanceState) {
	//
	// return super.onCreateDialog(savedInstanceState);
	// }

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		try {
			listener = (DialogListener) activity;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		indeterminate = true;
		if (savedInstanceState != null) {

			title = savedInstanceState.getString(FIELD_TITLE_NAME);
			message = savedInstanceState.getString(FIELD_MESSAGE_NAME);
			incrementMode = savedInstanceState.getBoolean(
					FIELD_INCREMENT_MODE_NAME, false);
			indeterminate = savedInstanceState.getBoolean(
					FIELD_INDETERMINATE_NAME, true);
			max = savedInstanceState.getInt(FIELD_MAX_NAME);
			progress = savedInstanceState.getInt(FIELD_PROGRESS_NAME);
		}

		setCancelable(true);

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
				listener.onCloseDialog(DIALOG_PROGRESS_ID, BUTTON_CANCEL);
			}
		});

		progressBar.setMax(max);
		progressBar.setProgress(progress);
		progressBar.setIndeterminate(indeterminate);

		setStyle(style, theme);

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		outState.putString(FIELD_TITLE_NAME, title);
		outState.putString(FIELD_MESSAGE_NAME, message);
		outState.putBoolean(FIELD_INCREMENT_MODE_NAME, incrementMode);
		outState.putBoolean(FIELD_INDETERMINATE_NAME, indeterminate);
		outState.putInt(FIELD_MAX_NAME, max);
		outState.putInt(FIELD_PROGRESS_NAME, progress);
	}

	@Override
	public void onDestroyView() {

		// ������� ������������� ����� ������� �� ������� ����:
		// https://code.google.com/p/android/issues/detail?id=17423
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);

		super.onDestroyView();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		listener = null;

		title = null;
		message = null;

		textViewTitle = null;
		textViewMessage = null;
		progressBar = null;
		buttonCancel = null;
	}

	@Override
	public void onCancel(DialogInterface dialog) {

		super.onCancel(dialog);

		listener.onCloseDialog(DIALOG_PROGRESS_ID, BUTTON_CANCEL);
	}

	// ��-�� ���� ������ ����� ��������� ������ � ������� onActivityResult().
	// ������� ������� � ���������� ������ show().
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
	// ���������.
	/**
	 * ��������� ���������.
	 * 
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {

		// Message.show(this);

		// ����� �.�. ������, ����� ��������� ������� ��� ������, �� ������
		// ������� ��� ��� (��������, � ������� onCreate ������������
		// ����������), ����� ������������ ������ ��������� �������� ����, �
		// ����� ��� ��� �� ��������������� - �� ����� ���������� �����, �����
		// �������� � ������� onCreateView().
		// ���� �� ����� ������, ����� ����� ������� ��� ������������ �
		// ����������, �� ����� ����� ���������� �������� � ��� ���� � ���
		// ������ ���.

		this.title = title;

		if (textViewTitle != null)
			textViewTitle.setText(this.title);
	}

	/**
	 * ��������� ���������.
	 * 
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {

		// Message.show(this);

		// ����� �.�. ������, ����� ��������� ������� ��� ������, �� ������
		// ������� ��� ��� (��������, � ������� onCreate ������������
		// ����������), ����� ������������ ������ ��������� �������� ����, �
		// ����� ��� ��� �� ��������������� - �� ����� ���������� �����, �����
		// �������� � ������� onCreateView().
		// ���� �� ����� ������, ����� ����� ������� ��� ������������ �
		// ����������, �� ����� ����� ���������� �������� � ��� ���� � ���
		// ������ ���.

		this.message = message;

		if (textViewMessage != null)
			textViewMessage.setText(this.message);
	}

	/**
	 * ��������� ������������� �������� ����������.
	 * 
	 * @param max
	 *            the maxValue to set
	 */
	public void setMax(int max) {

		this.max = max;
		if (progressBar != null)
			progressBar.setMax(this.max);
	}

	/**
	 * ��������� ������ ����������, ��� ������� �������� ���������
	 * ���������������� �� ��� ���������� �������� ���������, � ��� ����������
	 * ������������� �������� ���������.
	 * 
	 * @param progress
	 *            the currentValue to set
	 */
	public void setIncrementMode(boolean incrementMode) {
		this.incrementMode = incrementMode;
	}

	/**
	 * ��������� �������� �������� ����������.
	 * 
	 * @param nprogress
	 *            the currentValue to set
	 */
	public void setProgress(int newProgress) {

		int resultProgress = 0;
		if (incrementMode) {
			resultProgress = this.progress + newProgress;
		} else {
			resultProgress = newProgress;
		}

		this.progress = resultProgress;
		if (this.progress > this.max) {
			setMax(this.progress);
		}

		if (progressBar != null)
			progressBar.setProgress(this.progress);

	}

	/**
	 * ��������� �������� �������� ���������� "������������"/"��������������".
	 * 
	 * @param progress
	 *            the currentValue to set
	 */
	public void setIndeterminate(boolean value) {

		this.indeterminate = value;

		// ��������� ����� �������� ���������� ������� �������� ���������� �
		// ����!
		if (progressBar != null) {
			progressBar.setIndeterminate(this.indeterminate);
		}
	}

	/**
	 * ��������� �����. ����� ����� ����� ������ �� ������� onCreate()
	 * ������������ ����������, ����� onCreateView() ��� �� ������.
	 * 
	 * @param style
	 *            the style to set
	 */
	public void setStyle(int style) {
		this.style = style;
	}

	/**
	 * ��������� ���� ����������. ����� ����� ����� ������ �� ������� onCreate()
	 * ������������ ����������, ����� onCreateView() ��� �� ������.
	 * 
	 * @param theme
	 *            the theme to set
	 */
	public void setTheme(int theme) {
		this.theme = theme;
	}
}
