package com.jeztek.imok;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	private EditTextPreference mUserKeyPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setPreferenceScreen(createSettingsHierarchy());
	}

	private PreferenceScreen createSettingsHierarchy() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String userKey = settings.getString(Settings.USER_KEY, "0");

		// Root
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		
        // User settings
        PreferenceCategory userCategory = new PreferenceCategory(this);
        userCategory.setTitle(R.string.settings_category_user);
        root.addPreference(userCategory);

        // User key
        mUserKeyPref = new EditTextPreference(this);
        mUserKeyPref.setKey(Settings.USER_KEY);
        mUserKeyPref.setDialogTitle(R.string.settings_user_key_title);
        mUserKeyPref.setTitle(R.string.settings_user_key_title);
        mUserKeyPref.setSummary(userKey);
        mUserKeyPref.setOnPreferenceChangeListener(this);
        userCategory.addPreference(mUserKeyPref);

        // Reset settings
        Preference resetPref = new Preference(this);
        resetPref.setKey(Settings.RESET);
        resetPref.setTitle(R.string.settings_reset_title);
        resetPref.setOnPreferenceClickListener(this);
        userCategory.addPreference(resetPref);
        
		return root;
	}
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		preference.setSummary((String)newValue);
		return true;
	}

	public boolean onPreferenceClick(Preference preference) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Settings.USER_KEY, "0");
        editor.commit();            

        mUserKeyPref.setSummary("0");
        
		return true;
	}
}
