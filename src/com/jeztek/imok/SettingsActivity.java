package com.jeztek.imok;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity 
	implements OnPreferenceChangeListener, OnPreferenceClickListener {
	
	public static final String TAG = "ImOk.SettingsActivity";
	private static final String BROADCAST_SMS = "com.jeztek.imok.settings.BROADCAST_SMS";
		
	private static final int DIALOG_ENSURE = 1;
	private static final int DIALOG_INSTRUCT = 2;
	private static final int DIALOG_ERROR = 3;
	
	private Preference mVerifyPreference;
	private boolean mVerified = false;
	
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
		
		mVerifyPreference = findPreference(Settings.GATEWAY_VERIFY);
		mVerifyPreference.setOnPreferenceClickListener(this);
		mVerified = mVerifyPreference.getSharedPreferences().getBoolean(Settings.GATEWAY_VERIFY, mVerified);
	}
	
	@Override
	protected void onPause() {
		mVerifyPreference
			.getEditor().putBoolean(Settings.GATEWAY_VERIFY, mVerified)
			.commit();
		
		if (mReceiverRegistered)
			unregisterReceiver(mSmsSent);
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (mVerified == true) {
			mVerifyPreference.setSummary(getText(R.string.settings_gateway_verified));
		} else {
			mVerifyPreference.setSummary(getText(R.string.settings_gateway_unverified));
		}
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d("SettingsActivity", "Preference changed");
		if (preference.getKey().equals(Settings.GATEWAY_PHONE)) {
			EditTextPreference gatewayPhone = (EditTextPreference) preference;
			gatewayPhone.setSummary((String) newValue);
		}
		
		return true;
	}
	
	public boolean onPreferenceClick(Preference preference) {
		showDialog(DIALOG_ENSURE);
		return true;
	}
	
	private static final int ENSURE_MESSAGE_CANCEL = 1;
	private static final int ENSURE_MESSAGE_VERIFY = 2;
	private static final int ENSURE_MESSAGE_VERIFIED = 3;
	
	private final Handler mEnsureHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ENSURE_MESSAGE_VERIFY:
				verify();
				break;
				
			case ENSURE_MESSAGE_VERIFIED:
				mVerified = true;
				mVerifyPreference.getSharedPreferences().edit().putBoolean(Settings.GATEWAY_VERIFY, mVerified).commit();
				mVerifyPreference.setSummary(getText(R.string.settings_gateway_verified));
				break;
			}
		}
	};
	
	private final Message mVerifyMessage = new Message();
	private final Message mVerifiedMessage = new Message();
	private final Message mCancelMessage = new Message();
	
	@Override
	protected Dialog onCreateDialog(int id) {
		mVerifyMessage.what = ENSURE_MESSAGE_VERIFY;
		mVerifyMessage.setTarget(mEnsureHandler);
		
		mVerifiedMessage.what = ENSURE_MESSAGE_VERIFIED;
		mVerifiedMessage.setTarget(mEnsureHandler);
		
		mCancelMessage.what = ENSURE_MESSAGE_CANCEL;
		mCancelMessage.setTarget(mEnsureHandler);		
		
		switch (id) {
		case DIALOG_ENSURE:
			AlertDialog.Builder ensureBuilder = new AlertDialog.Builder(this);
			AlertDialog ensureDialog = ensureBuilder
				.setMessage(R.string.settings_ensure_message_unverified)
				.create();
			ensureDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", mVerifyMessage);
			ensureDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", mCancelMessage);
			ensureDialog.setCancelMessage(mCancelMessage);
			ensureDialog.setDismissMessage(mCancelMessage);			
			return ensureDialog;
			
		case DIALOG_INSTRUCT:
			AlertDialog.Builder instructBuilder = new AlertDialog.Builder(this);
			AlertDialog instructDialog = instructBuilder
				.setTitle(R.string.settings_dialog_instruct_title)
				.setMessage(R.string.settings_dialog_instruct_message)
				.create();
			instructDialog.setButton(Dialog.BUTTON_NEUTRAL, "Ok", mVerifiedMessage);
			return instructDialog;
		case DIALOG_ERROR:
			AlertDialog.Builder errorBuilder = new AlertDialog.Builder(this);
			return errorBuilder
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.settings_dialog_error_title)
				.setMessage(R.string.settings_dialog_error_message)
				.setNeutralButton("Ok", null)
				.create();
		}
		
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ENSURE:
			AlertDialog alertDialog = (AlertDialog) dialog;
			if (mVerified) {
				alertDialog.setMessage(getText(R.string.settings_ensure_message_verified));
			} else {
				alertDialog.setMessage(getText(R.string.settings_ensure_message_unverified));
			}
			return;
		}
		super.onPrepareDialog(id, dialog);
	}
	
	private boolean mReceiverRegistered = false;
	private BroadcastReceiver mSmsSent = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (this.getResultCode() == Activity.RESULT_OK) {
				showDialog(DIALOG_INSTRUCT);
			} else {
				showDialog(DIALOG_ERROR);
			}
			
			SettingsActivity.this.unregisterReceiver(mSmsSent);
			mReceiverRegistered = false;
		}
	};

	private void verify() {
		Log.d(TAG, "Sending verification SMS");
		
		registerReceiver(mSmsSent, new IntentFilter(BROADCAST_SMS));
		mReceiverRegistered = true;
		
		String number = 
			mVerifyPreference.getSharedPreferences().getString(Settings.GATEWAY_PHONE, "650-691-5005");
		
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(BROADCAST_SMS), 0);
		smsManager.sendTextMessage(number, null, "VERIFY", pendingIntent, null);
		
		Toast.makeText(this, "SMS Sent", Toast.LENGTH_SHORT).show();
	}
}
