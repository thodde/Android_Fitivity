package com.fitivity;

import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.PushService;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateActivityActivity extends Activity {
	Dialog dialog;
	ImageView addActivity;
	TextView activityText;
	ImageView addLocation;
	TextView locationText;
    public static final int ACTIVITY_REQUEST = 666;
    public static final int LOCATION_REQUEST = 999;
    Boolean locationSelected = false;
    Boolean activitySelected = false;
    
    Place activityLocation;
    FitivityActivity activityActivity;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);
		
		addActivity = (ImageView) findViewById(R.id.add_activity);
		addLocation = (ImageView) findViewById(R.id.add_location);
		
		activityText = (TextView) findViewById(R.id.activity_text);
		locationText = (TextView) findViewById(R.id.location_text);
		
		// Set Click Listeners
		addActivity.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				Intent mainIntent = new Intent(CreateActivityActivity.this, ChooseFitivityActivity.class);
                CreateActivityActivity.this.startActivityForResult(mainIntent, ACTIVITY_REQUEST);
			}
		});
		
		addLocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				Intent mainIntent = new Intent(CreateActivityActivity.this, LocationsActivity.class);
                CreateActivityActivity.this.startActivityForResult(mainIntent, LOCATION_REQUEST);
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        moveTaskToBack(true);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case (ACTIVITY_REQUEST) : { 
	      if (resultCode == Activity.RESULT_OK) { 
	    	 String activity = data.getStringExtra("activity");
	    	 
	    	 //Trim the extra crap off of the activity name
	    	 if(activity.contains("subcategory=")) {
	    		 activity = activity.substring(13, (activity.length()-1));
	    	 }
	    	 
	    	 activityActivity = new FitivityActivity(activity);
	    	 activitySelected = true;
	    	 
	    	 addActivity.setImageResource(R.drawable.choose_backplate);
	    	 activityText.setText(activityActivity.getName());
	    	 
	    	 if (activityActivity.getName().length() > 16) {
	    		  String text = activityActivity.getName().substring(0, 12);
	    		  text += "...";
	    		  activityText.setText(text);
	    	  }
	    	  else {
	    		  activityText.setText(activityActivity.getName());
	    	  }
	      } 
	      break; 
	    }
	    case (LOCATION_REQUEST) : { 
		      if (resultCode == Activity.RESULT_OK) { 
		    	  activityLocation = new Place();
		    	  
		    	  activityLocation.name = data.getStringExtra("name");
		    	  
		    	  Location location = new Location(LocationManager.NETWORK_PROVIDER);
		    	  location.setLatitude(data.getDoubleExtra("latitude", 0));
		    	  location.setLongitude(data.getDoubleExtra("longitude", 0));
		    	  
		    	  activityLocation.location = location;
		    	  
		    	  locationSelected = true;
		    	  
		    	  addLocation.setImageResource(R.drawable.choose_backplate);
		    	  
		    	  if (activityLocation.name.length() > 16) {
		    		  String text = activityLocation.name.substring(0, 12);
		    		  text += "...";
		    		  locationText.setText(text);
		    	  }
		    	  else {
		    		  locationText.setText(activityLocation.name);
		    	  }
		    	  
		      // TODO Update your TextView.
		      }
		      break;
	      }
	  }
	  
	  if (activitySelected == true && locationSelected == true) {
		  ParseQuery query = new ParseQuery("Groups");
		  
		  final ParseGeoPoint point = new ParseGeoPoint(activityLocation.location.getLatitude(), activityLocation.location.getLongitude());
		  
		  query.whereWithinMiles("location", point, 0.1);
		  //query.whereEqualTo("location", point);
		  query.whereEqualTo("place", activityLocation.name);
		  query.whereEqualTo("activity", activityActivity.getName());
		
		  query.getFirstInBackground(new GetCallback() {
			  ProgressDialog pd = ProgressDialog.show(CreateActivityActivity.this, "Saving...", "Creating activity", true, false);
			  public void done(ParseObject object, ParseException e) {
				    if (object == null) {
				    	Log.d("score", "The getFirst request failed.");
					
						/* Create the group */
						ParseObject group = new ParseObject("Groups");
						group.put("activity", activityActivity.getName());
						group.put("location", point);
						group.put("place", activityLocation.name);
						
						/* Create the event */
						ParseObject event = new ParseObject("ActivityEvent");
						event.put("creator", ParseUser.getCurrentUser());
						event.put("group", group);
						event.put("type", "NORMAL");
						event.put("status", "NEW");
						event.put("number", 1);
						
					    ParsePush push = new ParsePush();
					    push.setPushToIOS(true);
					    push.setMessage(ParseUser.getCurrentUser() + " created a new Activity!");
					    push.sendInBackground();
						
					    try {
							event.save();
							
							/*Create the group member */
							ParseObject member = new ParseObject("GroupMembers");
							member.put("user", ParseUser.getCurrentUser());
							member.put("activity", activityActivity.getName());
							member.put("location", point);
							member.put("place", activityLocation.name);
							member.save();
							
							ParseQuery query = new ParseQuery("Groups");
						    //query.whereEqualTo("group", object);
						      
						    query.whereWithinMiles("location", point, 0.1);
							//query.whereEqualTo("location", point);
							query.whereEqualTo("place", activityLocation.name);
							  
							query.whereEqualTo("activity", activityActivity.getName());
						      
						    try {
						    	ParseObject obj = query.getFirst();
						    	String channel = "Fitivity" + obj.getObjectId();
						    	PushService.subscribe(getApplicationContext(), channel, GroupActivity.class);
							}
						    catch (ParseException e1) {
						    	e1.printStackTrace();
							}
							
						}
					    catch (ParseException e1) {
							e1.printStackTrace();
							return;
						}
				    }
				    else {
				    	Log.d("score", "Retrieved the object.");
					      
					    ParseQuery memberQuery = new ParseQuery("GroupMembers");
					    memberQuery.whereWithinMiles("location", point, 0.1);
					    memberQuery.whereEqualTo("place", activityLocation.name);
					    memberQuery.whereEqualTo("activity", activityActivity.getName());
					    memberQuery.whereEqualTo("user", ParseUser.getCurrentUser());
					      
					    ParseObject membership = null;
					      
					    try {
					    	membership = memberQuery.getFirst();
					    }
					    catch(ParseException e1) {
					    	
					    }
					      
					    if (membership == null) {
						    ParseQuery query = new ParseQuery("Groups");
						    //query.whereEqualTo("group", object);
						      
						    query.whereWithinMiles("location", point, 0.1);
						    //query.whereEqualTo("location", point);
							query.whereEqualTo("place", activityLocation.name);
							  
							query.whereEqualTo("activity", activityActivity.getName());
						      
							ParseQuery q = new ParseQuery("ActivityEvent");
							q.whereMatchesQuery("group", query);
						      
							try {
								ParseObject event = q.getFirst();
						    	event.increment("number");
						    	event.put("status", "OLD");
						    	event.save();
						    	  
						    	/*Create the group member */
								ParseObject member = new ParseObject("GroupMembers");
								member.put("user", ParseUser.getCurrentUser());
								member.put("activity", activityActivity.getName());
								member.put("location", point);
								member.put("place", activityLocation.name);
								member.save();
						    	  
						    	ParseObject obj = event.getParseObject("group");
						    	String channel = "Fitivity" + obj.getObjectId();
						    	PushService.subscribe(getApplicationContext(), channel, GroupActivity.class);
							}
						    catch (ParseException e1) {
								e1.printStackTrace();
						    }
						}  
				    }
				    
				    pd.dismiss();
				    
				    Intent intent = new Intent();
					intent.setClass(CreateActivityActivity.this, GroupActivity.class);

					Bundle bundle = new Bundle();
				    bundle.putString("activityText", activityText.getText().toString());
				    bundle.putString("locationText", locationText.getText().toString());
				    bundle.putDouble("latitude", activityLocation.location.getLatitude());
				    bundle.putDouble("longitude", activityLocation.location.getLongitude());
				    
				    activityText.setText("");
				    locationText.setText("");
				    addLocation.setImageResource(R.drawable.choose_location);
				    addActivity.setImageResource(R.drawable.choose_activity);
				    activitySelected = false;
				    locationSelected = false;
				    
				    intent.putExtras(bundle);
				    startActivity(intent);
				  } 
			});	  
	    }
	}
}