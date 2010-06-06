package com.jeztek.imok;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SmsActivity extends Activity {
	public static final String TAG="ImOk.SmsActivity";
	public static final String BROADCAST_SMS = "com.jeztek.imok.SMS_SENT"; 
	
	private static final int DIALOG_ERROR = 1;
	private static final int DIALOG_EMERGENCY = 2;
	private static final int DIALOG_SENDING = 3;
	private static final int DIALOG_SENT = 4;
	
	private Location mLocation = null;
	
	private boolean mHaveProvider = false;
	private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            SmsActivity.this.mLocation = location;            
        }

		public void onProviderDisabled(String provider) { }
		public void onProviderEnabled(String provider) { }
		public void onStatusChanged(String provider, int status, Bundle extras) { }
    };
    
    private BroadcastReceiver mSentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_SMS)) {
				if (this.getResultCode() != Activity.RESULT_OK) {
					Log.e(TAG, "Error sending SMS");
					showDialog(DIALOG_ERROR);
					return;
				}
				
				dismissDialog(DIALOG_SENDING);
				showDialog(DIALOG_SENT);
			}
		}
    };
    
    private TextWatcher mLengthWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			mEditLength = mLocationEdit.getText().length()
				+ mFreeEdit.getText().length();
			
			// Add in <sp>#loc<sp> tag
			if (mLocationEdit.getText().length() > 0) {
				mEditLength += 6;
			}
			if (mFreeEdit.getText().length() > 0)
				mEditLength++;
			
			updateRemaining();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) 
		{ }

		public void onTextChanged(CharSequence s, int start, int before,
				int count) 
		{ }
	};
    
    private TextView mTextView;
    private long mHashLength;
    private TextView mCharsRemaining;
    
    private EditText mLocationEdit;
    private EditText mFreeEdit;
    
    private Button mSendButton;
    
    private static final int SMS_LENGTH = 160;
    private int mEditLength = 0;
    
    private boolean mDisclaimerShown = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SmsActivity onCreate called");

        setContentView(R.layout.sms);
        
    	mTextView = (TextView) findViewById(R.id.sms_msg);
    	mCharsRemaining = (TextView) findViewById(R.id.sms_remaining_msg);
    	
    	mFreeEdit = (EditText) findViewById(R.id.sms_free_txt);
    	mLocationEdit = (EditText) findViewById(R.id.sms_location_txt);
    	
    	mLocationEdit.addTextChangedListener(mLengthWatcher);
    	mFreeEdit.addTextChangedListener(mLengthWatcher);
    	
    	mSendButton = (Button) findViewById(R.id.sms_send_btn);
    	mSendButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			sendSms();
    		}
    	});
    	
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }
    
    @Override
	protected void onStart() { 
        super.onStart();
        Log.d(TAG, "SmsActivity onStart called");
        
        Intent i = this.getIntent();
        if (i.getAction().equals(Settings.SMS_ACTION_HELP)) {
        	setTitle(R.string.sms_help_name);
        	mTextView.setText(Html.fromHtml(getResources().getString(R.string.sms_needhelp_text)));
        	mHashLength = Settings.HASH_HELP.length() + 1;
        	
  	      	SharedPreferences settings = getSharedPreferences(Settings.SETTINGS_FILE, 0);
  	      	mDisclaimerShown = settings.getBoolean(Settings.SETTINGS_DISCLAIMER, false);
        	
        	if (!mDisclaimerShown) {
        		showDialog(DIALOG_EMERGENCY);
        		mDisclaimerShown = true;
        	}
        } else {
        	setTitle(R.string.sms_ok_name);
        	mTextView.setText(Html.fromHtml(getResources().getString(R.string.sms_ok_text)));
        	mHashLength = Settings.HASH_OK.length() + 1;
        }
        
        updateRemaining();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "SmsActivity onStop called");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "SmsActivity onResume called");

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        
        String provider = mLocationManager.getBestProvider(criteria, true);   
        if (provider != null) {
        	mHaveProvider = true;
        	mLocationManager.requestLocationUpdates(provider, 1000, 1, mLocationListener);
        }
        
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_SMS);
		registerReceiver(mSentReceiver, intentFilter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "SmsActivity onPause called");
		
	    // We need an Editor object to make preference changes.
	    // All objects are from android.context.Context
	    SharedPreferences settings = getSharedPreferences(Settings.SETTINGS_FILE, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(Settings.SETTINGS_DISCLAIMER, mDisclaimerShown);

	    // Commit the edits!
	    editor.commit();
		
		if (mHaveProvider)
			mLocationManager.removeUpdates(mLocationListener);
		
		unregisterReceiver(mSentReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "SmsActivity onCreate called");
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_EMERGENCY:
			return 
				new AlertDialog.Builder(this)
					.setTitle(getString(R.string.sms_help_name))
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(getString(R.string.sms_911_text))
					.setCancelable(false)
					.setPositiveButton(R.string.sms_911_continue, 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) { 
							}
					})
					.setNegativeButton(R.string.sms_911_cancel, 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) { 
								SmsActivity.this.finish();
							}
					})
					.create();
		case DIALOG_ERROR:
			return 
				new AlertDialog.Builder(this)
					.setTitle(getString(R.string.sms_error_title))
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(getString(R.string.sms_error_message))
					.setCancelable(false)
					.setPositiveButton("Ok", 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
					})
					.create();
		case DIALOG_SENDING: {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Sending....");
            return progressDialog;
		}
		case DIALOG_SENT:
			return 
				new AlertDialog.Builder(this)
					.setTitle(getString(R.string.sms_sent_title))
					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(getString(R.string.sms_sent_message))
					.setCancelable(false)
					.setPositiveButton("Ok", 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								SmsActivity.this.finish();
							}
					})
					.create();
		}
		
		return null;
	}
	
	private void sendSms() {
		final StringBuilder sb = new StringBuilder();
		
		if (mFreeEdit.getText().length() > 0) {
			sb.append(mFreeEdit.getText());
		}
		
		Intent i = getIntent();
		if (i.getAction().equals(Settings.SMS_ACTION_OK)) {
			sb.append(" ").append(Settings.HASH_OK);
		} else {
			sb.append(" ").append(Settings.HASH_HELP);
		}
		
		if (mLocationEdit.getText().length() > 0) {
			sb.append(" ").append(Settings.HASH_LOC).append(" ")
				.append(mLocationEdit.getText());
		}
		
		Log.d(TAG, "SMS to send: *" + sb.toString() + "*");
		
		showDialog(DIALOG_SENDING);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String phoneNumber = preferences.getString(
				Settings.GATEWAY_PHONE, 
				getText(R.string.settings_gateway_phone_summary).toString());

		/*
		DecimalFormat formatter = new DecimalFormat("###.######");
		String messageStr = "OK [" +
			formatter.format(mLocation.getLatitude()) +
			"," + 
			formatter.format(mLocation.getLongitude()) + "]";
			*/

		SmsManager manager = SmsManager.getDefault();
		Intent intent = new Intent(BROADCAST_SMS);
		PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		manager.sendTextMessage(phoneNumber, null, sb.toString(), sentIntent, null);
	}
	
	private void updateRemaining() {
		long charsRemaining = SMS_LENGTH - (mEditLength + mHashLength);
		
		String msg = String.format(getResources().getString(R.string.sms_remaining), charsRemaining);
		mCharsRemaining.setText(msg);
		
		if(charsRemaining < 0) {
			mCharsRemaining.setTextColor(0xffff0000);
			mSendButton.setEnabled(false);
		} else {
			mCharsRemaining.setTextColor(0xff000000);
			mSendButton.setEnabled(true);
		}
	}
}
