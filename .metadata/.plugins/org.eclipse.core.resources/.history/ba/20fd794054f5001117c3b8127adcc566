package com.Fitivity;



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
          // do stuff with the user
        	
        	/* Create a new handler with which to start the main activity
            and close this splash activity after SPLASH_DISPLAY_TIME has
            elapsed. */
        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
               	 
               	
                       
                        /* Create an intent that will start the main activity. */
                        Intent mainIntent = new Intent(SplashScreenActivity.this,
                                TabBarActivity.class);
                        SplashScreenActivity.this.startActivity(mainIntent);
                       
                        /* Finish splash activity so user cant go back to it. */
                        SplashScreenActivity.this.finish();
                       
                }
        }, SPLASH_DISPLAY_TIME);
        	
        } else {
          // show the signup or login screen
        	
        	/* Create a new handler with which to start the main activity
            and close this splash activity after SPLASH_DISPLAY_TIME has
            elapsed. */
        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
               	 
               	
                       
                        /* Create an intent that will start the main activity. */
                        Intent mainIntent = new Intent(SplashScreenActivity.this,
                                LoginActivity.class);
                        SplashScreenActivity.this.startActivity(mainIntent);
                       
                        /* Finish splash activity so user cant go back to it. */
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
