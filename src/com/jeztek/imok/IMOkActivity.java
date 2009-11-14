package com.jeztek.imok;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSettings();        
        setContentView(R.layout.main);
    
        Button imokButton = (Button)findViewById(R.id.imok_button);
        imokButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		reportImok();
        	}
        });
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
			showDialog(MENU_ABOUT);
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
		case MENU_ABOUT:
			return new AlertDialog.Builder(this)
			.setTitle(getString(R.string.imok_about_dialog_title))
			.setPositiveButton(R.string.imok_about_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.setMessage(getString(R.string.imok_about_dialog_message))
			.create();
		}
		return null;
	}
	
	private void reportImok() {
		
	}
	
	public boolean uploadData(Uri uri, long id, Map<String,String> vars) {
        try {
            boolean useSSL = false;
            if (Settings.SERVER_URL.startsWith("https")) {
            	useSSL = true;
            }

            HttpPost post = new HttpPost();
            String postUrl = Settings.SERVER_URL + "/data/imok/";
            Log.d(TAG, "Posting to URL " + postUrl);
            Map<String,String> temp = post.post(postUrl, useSSL, vars, "", null, null);
            int out = 200;
            Log.d(TAG, "POST response: " + (new Integer(out).toString()));
            
            return (out == 200);
        } 
        catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            return false;
        } 
        catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
            return false;
        }
        catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException: " + e);
            return false;
        }
    }
}