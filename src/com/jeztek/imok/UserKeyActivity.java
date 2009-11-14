package com.jeztek.imok;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserKeyActivity extends Activity {
	
	public static final String TAG = "IMOk.UserKeyActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.userkey);
		
		Button okButton = (Button)findViewById(R.id.userkey_button);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText userKeyText = (EditText)findViewById(R.id.userkey_edittext);
			}
		});
	}
}
