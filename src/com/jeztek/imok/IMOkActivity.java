package com.jeztek.imok;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class IMOkActivity extends Activity {
	
	public static final String DEBUG_ID = "IMOk.IMOkActivity";

	public static final String SETTINGS_SERVERURL = "http://localhost:8000";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public boolean uploadData(Uri uri, long id, Map<String,String> vars) {
        try {
            boolean useSSL = false;
            if (SETTINGS_SERVERURL.startsWith("https")) {
            	useSSL = true;
            }

            HttpPost post = new HttpPost();
            String postUrl = SETTINGS_SERVERURL + "/data/post/";
            Log.d(DEBUG_ID, "Posting to URL " + postUrl);
            int out = post.post(postUrl, useSSL, vars, "", null, null);            
            Log.d(DEBUG_ID, "POST response: " + (new Integer(out).toString()));
            
            return (out == 200);
        } 
        catch (FileNotFoundException e) {
            Log.e(DEBUG_ID, "FileNotFoundException: " + e);
            return false;
        } 
        catch (IOException e) {
            Log.e(DEBUG_ID, "IOException: " + e);
            return false;
        }
        catch (NullPointerException e) {
            Log.e(DEBUG_ID, "NullPointerException: " + e);
            return false;
        }
    }
}