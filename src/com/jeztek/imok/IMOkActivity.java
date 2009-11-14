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
    
        Button emergencyButton = (Button) findViewById(R.id.main_btn_911);
        emergencyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplication(), R.string.main_toast_calling, Toast.LENGTH_LONG).show();			
				// In reality we would actually call 911....
			}
		});
        
        Button reportButton = (Button) findViewById(R.id.main_btn_report);
        reportButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		SharedPreferences s = getApplication().getSharedPreferences(Settings.SETTINGS_FILE, Context.MODE_PRIVATE);
        		
        		Intent i = new Intent(getApplication(), ReportActivity.class);
        		startActivity(i);
        	}
        });
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuItem settingsItem = menu.add(0, MENU_SETUP, 0, R.string.main_menu_setup);
    	settingsItem.setIcon(android.R.drawable.ic_menu_manage);
    	
    	MenuItem aboutItem = menu.add(1, MENU_ABOUT, 0, R.string.main_menu_about);
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
		SharedPreferences.Editor editor = settings.edit();
		if (!settings.contains(Settings.FIRST_NAME)) {
			editor.putString(Settings.FIRST_NAME, "Tom");
		}			
		if (!settings.contains(Settings.LAST_NAME)) {			
			editor.putString(Settings.LAST_NAME, "Jones");
		}
		editor.commit();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case MENU_ABOUT:
			return new AlertDialog.Builder(this)
			.setTitle("I'm Ok!")
			.setPositiveButton(R.string.main_about_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.setMessage("Thanks for trying!")
			.create();
		}
		return null;
	}
	
	
	public boolean uploadData(Uri uri, long id, Map<String,String> vars) {
        try {
            boolean useSSL = false;
            if (Settings.SERVERURL.startsWith("https")) {
            	useSSL = true;
            }

            HttpPost post = new HttpPost();
            String postUrl = Settings.SERVERURL + "/data/post/";
            Log.d(TAG, "Posting to URL " + postUrl);
            int out = post.post(postUrl, useSSL, vars, "", null, null);            
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