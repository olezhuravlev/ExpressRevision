package pro.got4.expressrevision.dialogs;

import pro.got4.expressrevision.ItemsListFragmentActivity;
import pro.got4.expressrevision.R;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class NumberInputDialogFragment extends DialogFragment implements
		OnClickListener {

	public static final String TITLE_FIELD_NAME = "title_field";
	public static final String MESSAGE_FIELD_NAME = "message_field";
	public static final String INITIAL_VALUE_FIELD_NAME = "initial_value_field";

	private TextView message_TextView;
	private ImageButton up_ImageButton;
	private ImageButton down_ImageButton;
	private Button ok_Button;
	private Button cancel_Button;
	private EditText number_EditText;

	public static NumberInputDialogFragment newInstance(Bundle args) {

		NumberInputDialogFragment d = new NumberInputDialogFragment();
		d.setArguments(args);

		return d;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.dialog_input_number, null);

		message_TextView = (TextView) v.findViewById(R.id.message_TextView);
		up_ImageButton = (ImageButton) v.findViewById(R.id.up_ImageButton);
		down_ImageButton = (ImageButton) v.findViewById(R.id.down_ImageButton);
		ok_Button = (Button) v.findViewById(R.id.ok_Button);
		cancel_Button = (Button) v.findViewById(R.id.cancel_Button);
		number_EditText = (EditText) v.findViewById(R.id.number_EditText);

		up_ImageButton.setOnClickListener(this);
		down_ImageButton.setOnClickListener(this);
		ok_Button.setOnClickListener(this);
		cancel_Button.setOnClickListener(this);

		Bundle args = getArguments();
		if (args != null) {

			String title = args.getString(TITLE_FIELD_NAME);
			if (title != null) {

				// В случае использования диалога в качестве фрагмента функция
				// getDialog() может диалог не вернуть!
				// (возможно, состояние можно проверить через getShowsDialog()).
				Dialog dialog = getDialog();
				if (dialog != null)
					dialog.setTitle(title);
			}

			String message = args.getString(MESSAGE_FIELD_NAME);
			if (message != null) {
				message_TextView.setText(message);
			}

			Float initial_value = args.getFloat(INITIAL_VALUE_FIELD_NAME);
			if (initial_value != null) {
				number_EditText.setText(initial_value.toString());
			} else {
				number_EditText.setText("0.0");
			}
		}

		return v;
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.up_ImageButton:
			String val1 = incrementStringWithFloat(number_EditText.getText()
					.toString(), +1f, 0f, Float.MAX_VALUE);
			number_EditText.setText(val1);
			break;
		case R.id.down_ImageButton:
			String val2 = incrementStringWithFloat(number_EditText.getText()
					.toString(), -1f, 0f, Float.MAX_VALUE);
			number_EditText.setText(val2);
			break;
		case R.id.ok_Button:

			String st = number_EditText.getText().toString();
			Float value;
			try {
				value = Float.valueOf(st);
			} catch (NumberFormatException e) {
				value = 0f;
			}
			((ItemsListFragmentActivity) getActivity())
					.setCurrentQuantity(value);
			dismiss();
			break;

		case R.id.cancel_Button:
			dismiss();
			break;
		}
	}

	/**
	 * Приращение значения типа Float, хранящегося в типе String, на указанную
	 * величину.
	 * 
	 * @param value1
	 *            строка, хранящая Float
	 * @param value2
	 *            величина приращения Float
	 * @param minValue
	 *            минимально допустимое значение
	 * @param maxValue
	 *            максимально допустимое значение
	 * @return
	 */
	private String incrementStringWithFloat(String value1, Float value2,
			Float minValue, Float maxValue) {

		Float value;
		try {
			value = Float.valueOf(value1);// Float.parseFloat(value1)
		} catch (NumberFormatException e) {
			value = 0f;
		}

		value += value2;

		if (value <= minValue)
			value = minValue;

		if (value >= maxValue)
			value = maxValue;

		return value.toString();
	}
}
