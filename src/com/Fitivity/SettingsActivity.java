package com.fitivity;

import java.io.ByteArrayOutputStream;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.RequestPasswordResetCallback;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity {
	ParseUser user;
	Button logout;
	EditText email;
	EditText username;
	EditText password;
	ToggleButton pushNotificationButton;
	ImageView profilePicture;
	boolean hasPushNotifications;
	final int ACTIVITY_SELECT_IMAGE = 100;
	boolean resetOk;
	Bitmap myImage;
	Bitmap bmp;
	ParseObject largeImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl"); 
		
		setContentView(R.layout.settings_view);
		
		logout = (Button) findViewById(R.id.sign_out_button);
		email = (EditText) findViewById(R.id.emailText);
		username = (EditText) findViewById(R.id.usernameText);
		password = (EditText) findViewById(R.id.passwordText);
		pushNotificationButton = (ToggleButton) findViewById(R.id.pushNotificationButton);
		profilePicture = (ImageView) findViewById(R.id.imageView2);
				
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		hasPushNotifications = settings.getBoolean("PushNotifications", false);
		pushNotificationButton.setChecked(hasPushNotifications);
		
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
		
		password.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				resetOk = requestPasswordReset();
			}
		});
		
		pushNotificationButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			    @Override
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			        if(isChecked) {
			        	hasPushNotifications = true;
			        	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
			        	SharedPreferences.Editor editor = sharedPreferences.edit();
			    		editor.putBoolean("PushNotifications", hasPushNotifications);
			    		editor.commit();
			        }
			        else {
			        	hasPushNotifications = false;
			        	//PushService.unsubscribe(this, "Fitivity");
			        	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
			        	SharedPreferences.Editor editor = sharedPreferences.edit();
			    		editor.putBoolean("PushNotifications", hasPushNotifications);
			    		editor.commit();
			        }
			    }
		    });
		
		profilePicture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK,
			               android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
			}
		});
		
		user = ParseUser.getCurrentUser();
		String strEmail = user.getEmail();
		String strUsername = user.getUsername();
		email.setText(strEmail);
		username.setText(strUsername);

		ParseObject img = new ParseObject("image");
		ParseFile file = (ParseFile) img.get("ProfilePicture");
		
		if(file != null) {
			file.getDataInBackground(new GetDataCallback() {
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						// data[] will be your image
						Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
						profilePicture.setImageBitmap(bmp);
					}
					else {
						// something went wrong
					}
				}
			});
		}
		pushNotificationButton.setChecked(hasPushNotifications);
	}
	
	public Bitmap getProfilePicture() {
		ParseObject img = new ParseObject("image");
		ParseFile file = (ParseFile) img.get("ProfilePicture");
		
		if(file != null) {
			file.getDataInBackground(new GetDataCallback() {
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						// data[] will be your image
						bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
						profilePicture.setImageBitmap(bmp);
					}
					else {
						// something went wrong
					}
				}
			});
		}
		return bmp;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("PushNotifications", hasPushNotifications);
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putBoolean("PushNotifications", hasPushNotifications);
		editor.commit();
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	    hasPushNotifications = savedInstanceState.getBoolean("PushNotifications");
	    pushNotificationButton.setChecked(hasPushNotifications);
	    ParseObject img = new ParseObject("image");
		ParseFile file = (ParseFile) img.get("ProfilePicture");
		
		if(file != null) {
			file.getDataInBackground(new GetDataCallback() {
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						// object will be your image
						Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
						profilePicture.setImageBitmap(bmp);
					}
					else {
						// something went wrong
					}
				}
			});
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    switch(requestCode) { 
	    case ACTIVITY_SELECT_IMAGE:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = imageReturnedIntent.getData();
	            String[] filePathColumn = {MediaStore.Images.Media.DATA};

	            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	            cursor.moveToFirst();

	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String filePath = cursor.getString(columnIndex);
	            cursor.close();

	            Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
	            profilePicture.setImageBitmap(yourSelectedImage);
	            
	            ByteArrayOutputStream stream = new ByteArrayOutputStream();
	            boolean success = yourSelectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
	            if(success) {
	            	byte[] byteArray = stream.toByteArray();
	            	ParseFile file = new ParseFile("ProfilePicture.png", byteArray);
		            file.saveInBackground();
		            largeImage = new ParseObject("image");
		            largeImage.put("ProfilePicture", file);
	            }
	        }
	    }
	}
	
	public boolean requestPasswordReset() {
		ParseUser.requestPasswordResetInBackground(user.getEmail(), new RequestPasswordResetCallback() {
				@Override
				public void done(ParseException e) {
					if (e == null) {
						//An email was successfully sent with reset instructions.
						resetOk = true;
					}
					else {
						//Something went wrong.
						resetOk = false;
					}					
				}
		});
		return resetOk;
	}
}
