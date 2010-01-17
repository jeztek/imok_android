package com.jeztek.imok;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class IMOkActivity extends Activity {
	
	public static final String TAG = "IMOk.IMOkActivity";
	
	public static final int MENU_SETUP = 1;
	public static final int MENU_ABOUT = 2;
	
	public static final int DIALOG_ABOUT = 1;
	public static final int DIALOG_ACQUIRING = 2;
	
	private static final int MESSAGE_SHOW_DIALOG = 1;
	private static final int MESSAGE_HIDE_DIALOG = 2;
	private static final int MESSAGE_SHOW_TOAST = 3;
	
	private static final String MESSAGE_TOAST_TEXT = "toast_text";
	
	private Location mLocation = null;
	
	private boolean mHaveProvider = false;
	private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            IMOkActivity.this.mLocation = location;            
        }

		public void onProviderDisabled(String provider) { }
		public void onProviderEnabled(String provider) { }
		public void onStatusChanged(String provider, int status, Bundle extras) { }
    };
	
    private String mToastText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        Button imokButton = (Button) findViewById(R.id.imok_button);
        imokButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		reportImok();
        	}
        });
    }
    
    @Override
	protected void onStart() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        
        String provider = mLocationManager.getBestProvider(criteria, true);   
        if (provider != null) {
        	mHaveProvider = true;
        	mLocationManager.requestLocationUpdates(provider, 1000, 1, mLocationListener);
        }
        
        super.onStart();
	}

	@Override
	protected void onStop() {
		mLocationManager.removeUpdates(mLocationListener);
		
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	MenuItem settingsItem = menu.add(0, MENU_SETUP, 0, R.string.imok_menu_setup);
    	settingsItem.setIcon(android.R.drawable.ic_menu_preferences);
    	
    	MenuItem aboutItem = menu.add(1, MENU_ABOUT, 0, R.string.imok_menu_about);
    	aboutItem.setIcon(android.R.drawable.ic_menu_info_details);
		
    	return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showDialog(DIALOG_ABOUT);
			break;
		case MENU_SETUP:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ABOUT:
			return new AlertDialog.Builder(this)
			.setTitle(getString(R.string.imok_about_dialog_title))
			.setPositiveButton(R.string.imok_about_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.setMessage(getString(R.string.imok_about_dialog_message))
			.create();
			
		case DIALOG_ACQUIRING:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.setTitle("Sending status");
			progressDialog.setMessage("Acquiring position and sending...");
			return progressDialog;
		}
		return null;
	}
	
	private final Handler.Callback mHandler = new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SHOW_DIALOG:
				showDialog(DIALOG_ACQUIRING);
				return true;

			case MESSAGE_HIDE_DIALOG:
				dismissDialog(DIALOG_ACQUIRING);
				return true;
				
			case MESSAGE_SHOW_TOAST:
				Toast.makeText(IMOkActivity.this, (String) msg.obj, msg.arg1).show();
				return true;
			}
			
			return false;
		}
	};
	
	private void reportImok() {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		final Handler handler = new Handler(mHandler);
		
		Thread locationThread = new Thread(new Runnable() {
			public void run() {
				handler.sendEmptyMessage(MESSAGE_SHOW_DIALOG);
				
				if (mHaveProvider == true) {
					while(mLocation == null) { }
				}
				
				
				try {
					Thread.sleep(2000);
				} catch(InterruptedException e) {
					Log.e(TAG, "Hi");
				}
				
				
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(IMOkActivity.this);
				String phoneNumber = preferences.getString(
						Settings.GATEWAY_PHONE, 
						getText(R.string.settings_gateway_phone_summary).toString());
			
				DecimalFormat formatter = new DecimalFormat("###.######");
				String messageStr = "OK [" +
					formatter.format(mLocation.getLatitude()) +
					"," + 
					formatter.format(mLocation.getLongitude()) + "]";
			
				SmsManager manager = SmsManager.getDefault();
				manager.sendTextMessage(phoneNumber, null, messageStr, null, null);
				
				handler.sendEmptyMessage(MESSAGE_HIDE_DIALOG);
				
				Message message = new Message();
				message.arg1 = Toast.LENGTH_LONG;
				message.what = MESSAGE_SHOW_TOAST;
				message.obj = (Object) "SMS message sent";
				handler.sendMessage(message);
			}
		});
		locationThread.start();
		
		showDialog(DIALOG_ACQUIRING);
	}
	
	public Map<String,String> uploadData(String uri, Map<String,String> vars) {

        try {
            boolean useSSL = false;
            if (Settings.SERVER_URL.startsWith("https")) {
            	useSSL = true;
            }

            HttpPost post = new HttpPost();
            Log.d(TAG, "Posting to URL " + uri);
            Log.d(TAG, "Posting with args " + vars.toString());
            Map<String,String> response = post.post(uri, useSSL, vars, "", null, null);
            Log.d(TAG, "POST response: " + response.toString());
            
            return response;
        } 
        catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            return null;
        } 
        catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
            return null;
        }
        catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException: " + e);
            return null;
        }
    }
}