package com.jeztek.imok;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class UserKeyActivity extends Activity {
	
	public static final String TAG = "IMOk.UserKeyActivity";
	
	public static final int DIALOG_PROGRESS = 1;
	public static final int DIALOG_VALID = 2;
	public static final int DIALOG_INVALID = 3;
	
	private boolean mValid = false;
	private String mName;
	private String mKey;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.userkey);
		
		Button okButton = (Button)findViewById(R.id.userkey_button);
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EditText userKeyText = (EditText)findViewById(R.id.userkey_edittext);
				String userKey = userKeyText.getText().toString();

				if(testKey(userKey)) {
					if (mValid) {
						SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UserKeyActivity.this);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(Settings.USER_KEY, userKey);
						editor.commit();
						
						showDialog(DIALOG_VALID);
					} else {
						showDialog(DIALOG_INVALID);
					}
				} else {
					showDialog(DIALOG_INVALID);
				}
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.setTitle("Checking User Key");
			progressDialog.setMessage("Checking User Key...");
			return progressDialog;
		case DIALOG_VALID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Thank you for registering!")
			       .setCancelable(false)
			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                UserKeyActivity.this.finish();
			           }
			       });
			AlertDialog alert = builder.create();
			return alert;
		case DIALOG_INVALID:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setMessage("Invalid User Key. Are you sure you entered it correctly?")
			       .setCancelable(false)
			       .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			AlertDialog alert2 = builder2.create();
			return alert2;
		}
		
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_VALID:
			AlertDialog alertDialog = (AlertDialog) dialog;
			alertDialog.setMessage("Thank you, " + mName + ", for registering.");
		}
	}

	public boolean testKey(String userKey) {
        try {
            boolean useSSL = false;
            if (Settings.SERVER_URL.startsWith("https")) {
            	useSSL = true;
            }
          
            Map<String,String> temp = new HashMap<String,String>();
            HttpPost post = new HttpPost();
            String postUrl = Settings.SERVER_URL + Settings.URL_REGISTER + userKey + "/";
            Log.d(TAG, "Posting to URL " + postUrl);
            temp = post.post(postUrl, useSSL, temp, "", null, null);        
            Log.d(TAG, "POST response code: " + temp.get("code"));
            Log.d(TAG, "POST response message: " + temp.get("response"));
            
            mValid = false;
            
            try {
            	JSONObject o = new JSONObject(temp.get("response"));
            	if (o.getBoolean("result") == true) {
            		mName = o.getString("first_name") + " " + o.getString("last_name");
            		mValid = true;
            	}
            } catch(JSONException e) {
            	Log.d(TAG, "Invalid response from the server");
            	mValid = false;
            }
                        
            return (Integer.parseInt(temp.get("code")) == 200);
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
