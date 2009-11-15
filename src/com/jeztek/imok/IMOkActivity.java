package com.jeztek.imok;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.preference.PreferenceManager;
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

        loadSettings();        
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
    	settingsItem.setIcon(android.R.drawable.ic_menu_manage);
    	
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

	private void loadSettings() {
		// Load default preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (!settings.contains(Settings.USER_KEY)) {
			startActivity(new Intent(getApplication(), UserKeyActivity.class));
		}			
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
	
	private void reportImok() {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		final Handler handler = new Handler();
		
		Thread locationThread = new Thread(new Runnable() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						showDialog(DIALOG_ACQUIRING);
					}
				});
				
				if (mHaveProvider == true) {
					while(mLocation == null) { }
				}
				
				HashMap<String,String> location = new HashMap<String,String>();
				location.put("lat", Double.toString(mLocation.getLatitude()));
				location.put("lon", Double.toString(mLocation.getLongitude()));
				
				String url = Settings.SERVER_URL + Settings.URL_REPORT + settings.getString(Settings.USER_KEY, "") + "/";		
				Map<String,String> response = uploadData(url, location);
				
				if (Integer.parseInt(response.get("code")) != 200) {
					mToastText = "Error sending status. Pleas try again";
				} else {
					try {
						JSONObject o = new JSONObject(response.get("response"));
						if (o.getBoolean("result")) {
							mToastText = "Success. Your twitter status was also updated.";		
						} else {
							mToastText = "Success! Status updated.";
						}
					} catch (JSONException e) {
						mToastText = "Error sending status. Pleas try again";	
					}
				}

				handler.post(new Runnable() {
					public void run() {
						dismissDialog(DIALOG_ACQUIRING);
						Toast.makeText(IMOkActivity.this, mToastText, Toast.LENGTH_LONG).show();
					}
				});	
				
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