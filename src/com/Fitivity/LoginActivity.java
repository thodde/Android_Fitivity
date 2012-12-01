package com.fitivity;

import java.net.URL;
import org.json.JSONObject;

import com.fitivity.R;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class LoginActivity extends Activity {

	private EditText etUsername;
	private EditText etPassword;
	private ImageButton btnLogin;
	private ImageButton btnJoin;
	private ImageButton btnFacebook;
	private ProgressDialog pd;
	ParseUser user;
	final String facebookAppID = "119218824889348";
	ParseFile file;
	String response;
    String name = "";
    String strID = "";
    String userEmail = "";
    URL url;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");

		setContentView(R.layout.login_view);

		// Get the EditText and Button References
		etUsername = (EditText) findViewById(R.id.username);
		etPassword = (EditText) findViewById(R.id.password);
		btnLogin = (ImageButton) findViewById(R.id.login_button);
		btnJoin = (ImageButton) findViewById(R.id.signup_button);
		btnFacebook = (ImageButton) findViewById(R.id.facebook_signup_button);

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
								successHandler.sendMessage(successHandler.obtainMessage());
								/*
								 * Create an intent that will start the main
								 * activity.
								 */
								Intent mainIntent = new Intent(LoginActivity.this, TabBarActivity.class);
								LoginActivity.this.startActivity(mainIntent);

								LoginActivity.this.finish();
							} else if (user == null) {
								failHandler.sendMessage(failHandler.obtainMessage());
							}

						} catch (ParseException e) {
							connectionHandler.sendMessage(connectionHandler
									.obtainMessage());
						}
					}
				};

				pd = ProgressDialog.show(LoginActivity.this, "", "Loading...", true, false);
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
		ParseFacebookUtils.initialize("119218824889348", false);
		pd = ProgressDialog.show(LoginActivity.this, "", "Loading...", true, false);
		ParseFacebookUtils.logIn(this, new LogInCallback() {
			  @Override
			  public void done(ParseUser thisUser, ParseException err) {
				  user = thisUser;
				  new Thread() {
					  public void run() {
						  if (ParseUser.getCurrentUser() == null) {
						    	failHandler.sendMessage(failHandler.obtainMessage());
						    }
						    else if (ParseUser.getCurrentUser().isNew()) {
						    	successHandler.sendMessage(successHandler.obtainMessage());
						    	try {
						    		Bundle args = new Bundle();
							        args.putString("fields", "username");
							        JSONObject result = new JSONObject(ParseFacebookUtils.getFacebook().request("me", args));
							        ParseUser.getCurrentUser().setUsername(result.optString("username"));
							        
							        args.putString("fields", "email");
							        result = new JSONObject(ParseFacebookUtils.getFacebook().request("me", args));
							        ParseUser.getCurrentUser().setEmail(result.optString("email"));
							        
							        progressHandler.sendEmptyMessage(0);
							        ParseUser.getCurrentUser().saveInBackground();
						    	}
						    	catch (Exception e) {
						    		e.printStackTrace();
						    	}
						    	Intent mainIntent = new Intent(LoginActivity.this, TabBarActivity.class);
								LoginActivity.this.startActivity(mainIntent);
								LoginActivity.this.finish();
						    }
						    else {
						    	Intent mainIntent = new Intent(LoginActivity.this, TabBarActivity.class);
								LoginActivity.this.startActivity(mainIntent);
								LoginActivity.this.finish();
						  }
					  }
				  }.start();
			  }
		});
	}
	
	public void getCredentials() {
        	/*
            //get the profile picture
            url = new URL("http://graph.facebook.com/" + strID + "/picture?type=large");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            Bitmap mIcon1 = BitmapFactory.decodeStream(connection.getInputStream());

            int bytes = mIcon1.getWidth()*mIcon1.getHeight()*4;
			ByteBuffer buffer = ByteBuffer.allocate(bytes); 
			mIcon1.copyPixelsToBuffer(buffer); 
			byte[] array = buffer.array();
			file = new ParseFile("file", array);
			file.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) {
			        user = ParseUser.getCurrentUser();
			        user.put("image", file);
				}
			});
        */
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
