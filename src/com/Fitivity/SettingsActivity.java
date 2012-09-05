package com.fitivity;

import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SettingsActivity extends Activity {
	
	Button logout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings_view);
		
		logout = (Button) findViewById(R.id.sign_out_button);
		
		logout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ParseUser.logOut();
				Intent mainIntent = new Intent(SettingsActivity.this,
                        SplashScreenActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				startActivity(mainIntent);
			
			}
			
		});
		
	}
}
