package com.jeztek.imok;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity 
	implements OnPreferenceChangeListener {
	
	public static final String TAG = "ImOk.SettingsActivity";
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		EditTextPreference gatewayPhone = (EditTextPreference) findPreference(Settings.GATEWAY_PHONE);
		
		// Make it auto-format phone numbers
		gatewayPhone.getEditText().addTextChangedListener(new PhoneNumberFormattingTextWatcher());
		gatewayPhone.setText(PhoneNumberUtils.formatNumber(gatewayPhone.getText()));
		
		// Update summary automatically
		gatewayPhone.setOnPreferenceChangeListener(this);
		gatewayPhone.setSummary(gatewayPhone.getText());
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d("SettingsActivity", "Preference changed");
		if (preference.getKey().equals(Settings.GATEWAY_PHONE)) {
			EditTextPreference gatewayPhone = (EditTextPreference) preference;
			gatewayPhone.setSummary((String) newValue);
		}
		
		return true;
	}
}
