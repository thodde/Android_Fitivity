package com.fitivity;

import com.parse.Parse;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;


public class SplashScreenActivity extends Activity {
	
private static final int SPLASH_DISPLAY_TIME = 2000;  /* 2 seconds */
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl"); 
        
        // Splash screen view
        setContentView(R.layout.splash_view);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
	        new Handler().postDelayed(new Runnable() {
	                public void run() {
	                        Intent mainIntent = new Intent(SplashScreenActivity.this,
	                                TabBarActivity.class);
	                        SplashScreenActivity.this.startActivity(mainIntent);
	                       
	                        SplashScreenActivity.this.finish();
	                }
	        }, SPLASH_DISPLAY_TIME);
        	
        } 
        else {
            // show the login screen
	        new Handler().postDelayed(new Runnable() {
	                public void run() {
	                        Intent mainIntent = new Intent(SplashScreenActivity.this,
	                                LoginActivity.class);
	                        SplashScreenActivity.this.startActivity(mainIntent);
	                       
	                        SplashScreenActivity.this.finish();
	                }
	        }, SPLASH_DISPLAY_TIME);
        }
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        moveTaskToBack(true);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
