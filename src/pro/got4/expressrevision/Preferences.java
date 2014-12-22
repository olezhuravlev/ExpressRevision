package pro.got4.expressrevision;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Работа с экраном настроек, созданным на основе XML-описания.
 * 
 * @author programmer
 * 
 */
public class Preferences extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		// Preference chb1 = findPreference("chb1");
		// chb1.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		//
		// @Override
		// public boolean onPreferenceChange(Preference arg0, Object arg1) {
		// System.out.println("onPreferenceChange");
		// return false;
		// }
		// });
		//
		// chb1.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		//
		// @Override
		// public boolean onPreferenceClick(Preference preference) {
		// System.out.println("onPreferenceClick");
		// return false;
		// }
		// });
	}
}
