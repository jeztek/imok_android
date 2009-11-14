package com.jeztek.imok;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class IMOkActivity extends Activity {
	
	public static final String TAG = "IMOk.IMOkActivity";
	
	public static final int MENU_SETUP = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        		
        		if (!s.getBoolean(Settings.WIZARD_COMPLETE, false)) {
        			Intent i = new Intent(getApplication(), WizardActivity.class);
        			startActivity(i);
        			return;
        		}
        		
        		Intent i = new Intent(getApplication(), ReportActivity.class);
        		startActivity(i);
        	}
        });
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_SETUP, 0, R.string.main_menu_setup)
    		.setIcon(android.R.drawable.ic_menu_manage);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETUP:
			startActivity(new Intent(this, WizardActivity.class));
			return true;		
		}
		
		return false;
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