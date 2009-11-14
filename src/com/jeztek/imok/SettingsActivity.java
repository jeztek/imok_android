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

	private EditTextPreference mFirstNamePref;
	private EditTextPreference mLastNamePref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setPreferenceScreen(createSettingsHierarchy());
	}

	private PreferenceScreen createSettingsHierarchy() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String firstName = settings.getString(Settings.FIRST_NAME, "Tom");
		String lastName = settings.getString(Settings.LAST_NAME, "Jones");

		// Root
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		
        // User settings
        PreferenceCategory userCategory = new PreferenceCategory(this);
        userCategory.setTitle(R.string.settings_category_user);
        root.addPreference(userCategory);

        // First name
        mFirstNamePref = new EditTextPreference(this);
        mFirstNamePref.setKey(Settings.FIRST_NAME);
        mFirstNamePref.setDialogTitle(R.string.settings_user_firstname_title);
        mFirstNamePref.setTitle(R.string.settings_user_firstname_title);
        mFirstNamePref.setSummary(firstName);
        mFirstNamePref.setOnPreferenceChangeListener(this);
        userCategory.addPreference(mFirstNamePref);

        // Last name
        mLastNamePref = new EditTextPreference(this);
        mLastNamePref.setKey(Settings.LAST_NAME);
        mLastNamePref.setDialogTitle(R.string.settings_user_lastname_title);
        mLastNamePref.setTitle(R.string.settings_user_lastname_title);
        mLastNamePref.setSummary(lastName);
        mLastNamePref.setOnPreferenceChangeListener(this);
        userCategory.addPreference(mLastNamePref);

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
        editor.putString(Settings.FIRST_NAME, "Tom");
        editor.putString(Settings.LAST_NAME, "Jones");
        editor.commit();            

        mFirstNamePref.setSummary("Tom");
        mLastNamePref.setSummary("Jones");
        
		return true;
	}

}
