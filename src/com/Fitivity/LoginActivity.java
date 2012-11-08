package com.fitivity;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.fitivity.R;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.ParseFacebookUtils.Permissions.User;
import com.parse.facebook.FacebookError;
import com.parse.facebook.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private EditText etUsername;
	private EditText etPassword;
	private Button btnLogin;
	private Button btnJoin;
	private Button btnFacebook;
	private ProgressDialog pd;
	ParseUser user;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq",
				"krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");

		setContentView(R.layout.login_view);

		// Get the EditText and Button References
		etUsername = (EditText) findViewById(R.id.username);
		etPassword = (EditText) findViewById(R.id.password);
		btnLogin = (Button) findViewById(R.id.login_button);
		btnJoin = (Button) findViewById(R.id.signup_button);
		btnFacebook = (Button) findViewById(R.id.facebook_signup_button);

		// Set Click Listeners
		btnLogin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Check Login
				Thread loginThread = new Thread() {
					@Override
					public void run() {
						try {
							super.run();
							String username = etUsername.getText().toString();
							String password = etPassword.getText().toString();

							user = ParseUser.logIn(username, password);
							progressHandler.sendEmptyMessage(0);
							if (user != null) {
								successHandler.sendMessage(successHandler
										.obtainMessage());
								/*
								 * Create an intent that will start the main
								 * activity.
								 */
								Intent mainIntent = new Intent(
										LoginActivity.this,
										TabBarActivity.class);
								LoginActivity.this.startActivity(mainIntent);

								LoginActivity.this.finish();
							} else if (user == null) {
								failHandler.sendMessage(failHandler
										.obtainMessage());
							}

						} catch (ParseException e) {
							connectionHandler.sendMessage(connectionHandler
									.obtainMessage());
						}
					}
				};

				pd = ProgressDialog.show(LoginActivity.this, "", "Loading...",
						true, false);
				loginThread.start();

			}
		});

		btnJoin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent mainIntent = new Intent(LoginActivity.this,
						SignUpActivity.class);
				LoginActivity.this.startActivity(mainIntent);
			}
		});
		
		//allow the user to login with facebook
		btnFacebook.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setupFacebook();
			}
		});
	}
	
	public void setupFacebook() {
		ParseFacebookUtils.initialize("119218824889348");
		
		ParseFacebookUtils.logIn(this, new LogInCallback() {
			  @Override
			  public void done(ParseUser user, ParseException err) {
			    if (user == null) {
			      Log.i("MyApp", "Uh oh. The user cancelled the Facebook login.");
			    }
			    else if (user.isNew()) {
			      Log.i("MyApp", "User signed up and logged in through Facebook!");
			      Intent mainIntent = new Intent(
							LoginActivity.this,
							TabBarActivity.class);
					LoginActivity.this.startActivity(mainIntent);
					
					getCredentials();
					
					LoginActivity.this.finish();
			    }
			    else {
			      Log.i("MyApp", "User logged in through Facebook!");
					Intent mainIntent = new Intent(
							LoginActivity.this,
							TabBarActivity.class);
					LoginActivity.this.startActivity(mainIntent);
					
					getCredentials();
					
					LoginActivity.this.finish();
			    }
			  }
			});
	}
	
	public void getCredentials() {
        String response;
        String name = "";
        try {
            response = ParseFacebookUtils.getFacebook().request("me");
            JSONObject json = Util.parseJson(response);
            name = json.getString("first_name");
        } 
        catch (MalformedURLException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        catch (FacebookError e) {
            e.printStackTrace();
        } 
        catch (JSONException e) {
            e.printStackTrace();
        }

        user = ParseUser.getCurrentUser();
        user.setUsername(name);
        user.saveInBackground();
    }
	
	private Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update dialog here
			pd.dismiss();
		}
	};

	private Handler connectionHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert = "Sign-in didn't succeed.";
			Toast toast = Toast.makeText(LoginActivity.this, alert,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

			toast.show();
			pd.dismiss();
		}
	};

	private Handler successHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert = "Hooray! The user is logged in.";
			Toast toast = Toast.makeText(LoginActivity.this, alert,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
	};

	private Handler failHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert2 = "Sign in didn't succeed. The username or password was invalid.";
			Toast toast = Toast.makeText(LoginActivity.this, alert2,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

			toast.show();
			pd.dismiss();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
