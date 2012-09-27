package com.fitivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.fitivity.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SignUpActivity extends Activity {
	
	// Declare our Views, so we can access them later
	private ImageView imageView;
	private EditText etEmail;
	private EditText etUsername;
	private EditText etPassword;
	private EditText etConfirm;
	private Button btnSignup;
	private ProgressDialog pd;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl"); 
		
		setContentView(R.layout.signup_view);
		
		// Get the EditText and Button References
		etEmail = (EditText) findViewById(R.id.signupemail);
		etUsername = (EditText) findViewById(R.id.signupusername);
		etPassword = (EditText) findViewById(R.id.signuppassword);
		etConfirm = (EditText) findViewById(R.id.signupconfirm);
		btnSignup = (Button) findViewById(R.id.signup_button);
		
		etEmail.addTextChangedListener(new TextWatcher() { 
            public void afterTextChanged(Editable s) { 
            	String email = etEmail.getText().toString();
            	if (isEmailValid(email)){
            		imageView = (ImageView) findViewById(R.id.signupemailicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_on);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_on);     		
            	}
            	else {
            		imageView = (ImageView) findViewById(R.id.signupemailicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_off);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_off);
            	}
        } 
        public void beforeTextChanged(CharSequence s, int start, int count, 
int after) { 
                //XXX do something 
        } 
        public void onTextChanged(CharSequence s, int start, int before, int count) { 
                //XXX do something 
        } 
}); 
		
		
		etPassword.addTextChangedListener(new TextWatcher() { 
            public void afterTextChanged(Editable s) { 
            	String password = etPassword.getText().toString();
            	if ((password.length() >= 6) && (password.length() < 15)){
            		imageView = (ImageView) findViewById(R.id.signuppasswordicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_on);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_on);     		
            	}
            	else {
            		imageView = (ImageView) findViewById(R.id.signuppasswordicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_off);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_off);
            	}
        } 
        public void beforeTextChanged(CharSequence s, int start, int count, 
int after) { 
               
        } 
        public void onTextChanged(CharSequence s, int start, int before, int count) { 
                
        } 
}); 
		
		
		
		etUsername.addTextChangedListener(new TextWatcher() { 
            public void afterTextChanged(Editable s) { 
                //XXX do something 
            	String username = etUsername.getText().toString();
            	if ((username.length() >= 6) && (username.length() < 15)){
            		imageView = (ImageView) findViewById(R.id.signupusernameicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_on);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_on);     		
            	}
            	else {
            		imageView = (ImageView) findViewById(R.id.signupusernameicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_off);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_off);
            	}
        } 
        public void beforeTextChanged(CharSequence s, int start, int count, 
int after) { 
                //XXX do something 
        } 
        public void onTextChanged(CharSequence s, int start, int before, int count) { 
                //XXX do something 
        } 
});
		etConfirm.addTextChangedListener(new TextWatcher() { 
            public void afterTextChanged(Editable s) { 
                //XXX do something 
            	String confirm = etConfirm.getText().toString();
            	if ((confirm.length() >= 6) && (confirm.length() < 15) 
            			&& confirm.equals(etPassword.getText().toString())){
            		imageView = (ImageView) findViewById(R.id.signupconfirmicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_on);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_on);     		
            	}
            	else {
            		imageView = (ImageView) findViewById(R.id.signupconfirmicon);
            		//imageView.setImageResource(R.drawable.btn_check_buttonless_off);
            		imageView.setBackgroundResource(R.drawable.btn_check_buttonless_off);
            	}
        } 
        public void beforeTextChanged(CharSequence s, int start, int count, 
int after) { 
                //XXX do something 
        } 
        public void onTextChanged(CharSequence s, int start, int before, int count) { 
                //XXX do something 
        } 
});
		
		
		
		btnSignup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				Thread signupThread = new Thread() {
					@Override
					public void run() {
						try {
							super.run();
							
							String email = etEmail.getText().toString();
							String username = etUsername.getText().toString();
							String password = etPassword.getText().toString();
							String confirm = etConfirm.getText().toString();
							
							
							if (password.length() < 6){
								passwordfailHandler.sendMessage(passwordfailHandler.obtainMessage());
								return;
								
							}
							if (password.length() > 15){
								passwordfail2Handler.sendMessage(passwordfail2Handler.obtainMessage());
								return;
								
							}
							if (username.length() < 6){
								usernamefailHandler.sendMessage(usernamefailHandler.obtainMessage());
								return;
							}
							if (username.length() > 15){
								usernamefail2Handler.sendMessage(usernamefail2Handler.obtainMessage());
								return;
								
							}
							if (!confirm.equals(password)) {
								confirmfailHandler.sendMessage(confirmfailHandler.obtainMessage());
								return;
							}
							
							ParseUser user = new ParseUser();
							user.setEmail(email);
							user.setUsername(username);
							user.setPassword(password);
							
							user.signUp();
							progressHandler.sendEmptyMessage(0);
							
							
							 /* Create an intent that will start the main activity. */
	                         Intent mainIntent = new Intent(SignUpActivity.this,
	                                 TabBarActivity.class);
	                         SignUpActivity.this.startActivity(mainIntent);
	                         
	                        
	                         /* Finish splash activity so user cant go back to it. */
	                         SignUpActivity.this.finish();
							
						} catch (ParseException e) {
							failHandler.sendMessage(failHandler.obtainMessage());
						} 
						
					}
					
				};
				
				pd = ProgressDialog.show(SignUpActivity.this, "", "Loading...", true,
                        false);
				signupThread.start();
				
				
				
			}
			
			
		});
	}
	
	private Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update dialog here
			pd.dismiss();
		}
	};
	
	private Handler confirmfailHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert = "Your passwords don't match";
			pd.dismiss();
			Toast.makeText(SignUpActivity.this, alert, Toast.LENGTH_SHORT)
			.show();
		}
	};
	
	private Handler passwordfailHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert = "Your password must be at least 6 characters";
			pd.dismiss();
			Toast.makeText(SignUpActivity.this, alert, Toast.LENGTH_SHORT)
			.show();
		}
	};
	
	private Handler passwordfail2Handler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert = "Your password must be no longer than 15 characters";
			pd.dismiss();
			Toast.makeText(SignUpActivity.this, alert, Toast.LENGTH_SHORT)
			.show();
		}
	};
	
	private Handler usernamefailHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert = "Your username must be at least 6 characters";
			pd.dismiss();
			Toast.makeText(SignUpActivity.this, alert, Toast.LENGTH_SHORT)
			.show();
		}
	};
	
	private Handler usernamefail2Handler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert = "Your username must be no longer than 15 characters";
			pd.dismiss();
			Toast.makeText(SignUpActivity.this, alert, Toast.LENGTH_SHORT)
			.show();
		}
	};
	
	private Handler failHandler = new Handler() {
		public void handleMessage(Message msg) {
			// update your UI here
			String alert2 = "Please check your connection or try a different username.";
			Toast.makeText(SignUpActivity.this, alert2, Toast.LENGTH_SHORT)
			.show();
		}
	};
	
	public static boolean isEmailValid(String email){
		boolean isValid = false;

		//Initialize reg ex for email.
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		//Make the comparison case-insensitive.
		Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if(matcher.matches()){
		isValid = true;
		}
		return isValid;
		}

}
