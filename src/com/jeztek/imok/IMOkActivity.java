package com.jeztek.imok;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IMOkActivity extends Activity {
	
	public static final String TAG = "IMOk.IMOkActivity";
	
	private static final int MENU_SETUP = 1;
	private static final int MENU_ABOUT = 2;
	
	private static final int DIALOG_ABOUT = 1;
	
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        final Button imokButton = (Button) findViewById(R.id.imok_button);
        imokButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent i = new Intent(IMOkActivity.this.getApplicationContext(), SmsActivity.class);
        		i.setAction(Settings.SMS_ACTION_OK);
        		IMOkActivity.this.startActivity(i);
        	}
        });
        
        final Button helpButton = (Button) findViewById(R.id.imok_help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        	    // We need an Editor object to make preference changes.
        	    // All objects are from android.context.Context
        	    SharedPreferences settings = getSharedPreferences(Settings.SETTINGS_FILE, 0);
        	    SharedPreferences.Editor editor = settings.edit();
        	    editor.putBoolean(Settings.SETTINGS_DISCLAIMER, false);

        	    // Commit the edits!
        	    editor.commit();
        		
        		Intent i = new Intent(IMOkActivity.this.getApplicationContext(), SmsActivity.class);
        		i.setAction(Settings.SMS_ACTION_HELP);
        		IMOkActivity.this.startActivity(i);
        	}
        });

        final TextView message = (TextView) findViewById(R.id.imok_message);
        message.setText(Html.fromHtml(getResources().getString(R.string.imok_text)));
    }
    
    @Override
	protected void onStart() {        
        super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        
        String provider = mLocationManager.getBestProvider(criteria, true);   
        if (provider != null) {
        	mHaveProvider = true;
        	mLocationManager.requestLocationUpdates(provider, 1000, 1, mLocationListener);
        }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (mHaveProvider)
			mLocationManager.removeUpdates(mLocationListener);	
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
			return 
				new AlertDialog.Builder(this)
					.setTitle(getString(R.string.imok_about_dialog_title))
					.setMessage(getString(R.string.imok_about_dialog_message))
					.setPositiveButton(R.string.imok_about_dialog_ok, 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) { }
					})
					.create();
		}
		
		return null;
	}
}