package com.fitivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.PushService;

import android.app.Activity;
import android.app.AlertDialog;
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
	
	String today;
	boolean underDailyLimit;
	public ParseObject firstGroup;
	public ParseObject fifthGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);

		addActivity = (ImageView) findViewById(R.id.add_activity);
		addLocation = (ImageView) findViewById(R.id.add_location);

		activityText = (TextView) findViewById(R.id.activity_text);
		locationText = (TextView) findViewById(R.id.location_text);
		
		//get todays date and the latest groups created by the user
		checkDailyCount();
		today = checkTodaysDate();

		// Set Click Listeners
		addActivity.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//make sure the nmber of groups is below 5
				checkNumberOfGroupsToday();				
				//if they are all set, create a group, otherwise, display a message saying they cannot create a new group
				if(underDailyLimit) {
					Intent mainIntent = new Intent(CreateActivityActivity.this, ChooseFitivityActivity.class);
					CreateActivityActivity.this.startActivityForResult(mainIntent, ACTIVITY_REQUEST);
				}
				else {
					//If the user has already created five groups today...
					AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(CreateActivityActivity.this);
					dlgAlert.setMessage("You have already created 5 groups today!");
					dlgAlert.setTitle("Fitivity");
					dlgAlert.setPositiveButton("OK", null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();
				}
			}
		});

		addLocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				checkNumberOfGroupsToday();
				if(underDailyLimit) {
					Intent mainIntent = new Intent(CreateActivityActivity.this, LocationsActivity.class);
					CreateActivityActivity.this.startActivityForResult(mainIntent, LOCATION_REQUEST);
				}
				else {
					//If the user has already created five groups today...
					AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(CreateActivityActivity.this);
					dlgAlert.setMessage("You have already created 5 groups today!");
					dlgAlert.setTitle("Fitivity");
					dlgAlert.setPositiveButton("OK", null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();
				}
			}
		});
	}
	
	/**
	 * Make sure the user has not already created more than 5 groups today
	 */
	public void checkNumberOfGroupsToday() {
		if(firstGroup != null && fifthGroup != null) {
			Date date1 = firstGroup.getCreatedAt();
			Date date5 = fifthGroup.getCreatedAt();
		
			if(date1 != null && date5 != null) {
				String strDate1 = date1.toString().substring(0, Math.min(date1.toString().length(), 10));
				String strDate5 = date5.toString().substring(0, Math.min(date5.toString().length(), 10));
				
				//if today does not match the last date, its a new day so we are under the limit
				//if the dates are equal, not allowed: return false
				if(!today.equals(strDate1)) {
					underDailyLimit = true;
				}
				else if(today.equals(strDate5)) {
					underDailyLimit = false;
				}
				else if(strDate1.equals(strDate5)) {
					underDailyLimit = false;
				}
				else {
					underDailyLimit = true;
				}
			}
		}
	}
	
	/**
	 * check the date today and convert it to a useable format
	 * @return String todays date
	 */
	public String checkTodaysDate() {
		//get a calendar object
		final Calendar c = Calendar.getInstance();
		//get the day of the week is string format
		int day = c.get(Calendar.DAY_OF_WEEK);
		String weekday = getCurrentDayOfWeek(day);
		//set up a formatter for the month and convert month to string format
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM");
		String monthOfYear = dateFormat.format(c.getTime());
		//get the day of the month
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		//put it all together to make a String comparable to the date pulled from Parse
		return ("" + weekday + " " + monthOfYear + " " + dayOfMonth);
	}
	
	/**
	 * Simple way to convert the current day of week
	 * to a String instead of a number 1 - 7
	 * @param day int between 1 and 7
	 * @return String day of the week, ex. Wed
	 */
	public String getCurrentDayOfWeek(int day) {
		String strDay = "Sun";
		
		switch(day) {
		case 1:
			strDay = "Sun";
		case 2:
			strDay = "Mon";
		case 3:
			strDay = "Tue";
		case 4:
			strDay = "Wed";
		case 5:
			strDay = "Thu";
		case 6:
			strDay = "Fri";
		case 7:
			strDay = "Sat";
		}
		
		return strDay;
	}
	
	/**
	 * Makes sure that the user has not already proposed more than 2 activities today.
	 * @return true if the user is still able to create another activity
	 */
	public void checkDailyCount() {
		ParseQuery query = new ParseQuery("ActivityEvent");
		query.orderByDescending("createdAt");
		query.whereEqualTo("creator", ParseUser.getCurrentUser());
		query.whereEqualTo("postType", 0);
		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					LinkedList<ParseObject> groups = new LinkedList<ParseObject>();
					for (int i = 0; i < objects.size(); i++) {
						ParseObject activity = objects.get(i);
						groups.add(activity);
					}
					
					if(groups.size() < 5) {
						underDailyLimit = true;
					}
					else {
						firstGroup = groups.get(0);
						fifthGroup = groups.get(4);
						
						//if they are null, the user has not yet created 5 groups today
						if (firstGroup == null || fifthGroup == null) {
							underDailyLimit = true;
						}
					}
				}
				else {
					e.printStackTrace();
				}
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
		switch (requestCode) {
		case (ACTIVITY_REQUEST): {
			if (resultCode == Activity.RESULT_OK) {
				String activity = data.getStringExtra("activity");

				// Trim the extra crap off of the activity name
				if (activity.contains("subcategory=")) {
					activity = activity.substring(13, (activity.length() - 1));
				}

				activityActivity = new FitivityActivity(activity);
				activitySelected = true;

				addActivity.setImageResource(R.drawable.choose_backplate);
				activityText.setText(activityActivity.getName());

				if (activityActivity.getName().length() > 16) {
					String text = activityActivity.getName().substring(0, 12);
					text += "...";
					activityText.setText(text);
				} else {
					activityText.setText(activityActivity.getName());
				}
			}
			break;
		}
		case (LOCATION_REQUEST): {
			if (resultCode == Activity.RESULT_OK) {
				activityLocation = new Place();

				activityLocation.name = data.getStringExtra("locationName");

				Location location = new Location(
						LocationManager.NETWORK_PROVIDER);
				location.setLatitude(data.getDoubleExtra("latitude", 0));
				location.setLongitude(data.getDoubleExtra("longitude", 0));

				activityLocation.location = location;

				locationSelected = true;

				addLocation.setImageResource(R.drawable.choose_backplate);

				if (activityLocation.name.length() > 16) {
					String text = activityLocation.name.substring(0, Math.max(activityLocation.name.length(), 12));
					text += "...";
					locationText.setText(text);
				} else {
					locationText.setText(activityLocation.name);
				}
			}
			break;
		}
		}

		if (activitySelected == true && locationSelected == true) {
			ParseQuery query = new ParseQuery("Groups");

			final ParseGeoPoint point = new ParseGeoPoint(
					activityLocation.location.getLatitude(),
					activityLocation.location.getLongitude());

			query.whereWithinMiles("location", point, 0.1);
			query.whereEqualTo("place", activityLocation.name);
			query.whereEqualTo("activity", activityActivity.getName());

			query.getFirstInBackground(new GetCallback() {
				ProgressDialog pd = ProgressDialog.show(
						CreateActivityActivity.this, "Saving...",
						"Creating activity", true, false);

				public void done(ParseObject object, ParseException e) {
					if (object == null) {
						/* Create the group */
						ParseObject group = new ParseObject("Groups");
						group.put("activity", activityActivity.getName());
						group.put("location", point);
						group.put("place", activityLocation.name);
						group.put("activityCount", 0);

						/* Create the event */
						ParseObject event = new ParseObject("ActivityEvent");
						event.put("creator", ParseUser.getCurrentUser());
						event.put("group", group);
						event.put("number", 1);
						event.put("postType", 0);

						/* Save this for later
						ParsePush push = new ParsePush();
						push.setPushToIOS(true);
						push.setMessage(ParseUser.getCurrentUser() + " created a new Activity!");
						push.sendInBackground();
						*/

						try {
							event.save();

							/* Create the group member */
							ParseObject member = new ParseObject("GroupMembers");
							member.put("user", ParseUser.getCurrentUser());
							member.put("activity", activityActivity.getName());
							member.put("location", point);
							member.put("place", activityLocation.name);
							member.save();

							ParseQuery query = new ParseQuery("Groups");
							query.whereWithinMiles("location", point, 0.1);
							query.whereEqualTo("place", activityLocation.name);
							query.whereEqualTo("activity", activityActivity.getName());

							try {
								ParseObject obj = query.getFirst();
								String channel = "Fitivity" + obj.getObjectId();
								PushService.subscribe(getApplicationContext(),
										channel, GroupActivity.class);
							}
							catch (ParseException ex) {
								ex.printStackTrace();
							}

						} catch (ParseException e1) {
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
						catch (ParseException e1) {
							e.printStackTrace();
						}

						if (membership == null) {
							ParseQuery query = new ParseQuery("Groups");
							query.whereWithinMiles("location", point, 0.1);
							query.whereEqualTo("place", activityLocation.name);
							query.whereEqualTo("activity", activityActivity.getName());

							ParseQuery q = new ParseQuery("ActivityEvent");
							q.whereMatchesQuery("group", query);

							try {
								ParseObject event = q.getFirst();
								event.increment("number");
								event.save();

								/* Create the group member */
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
							catch (ParseException ex) {
								ex.printStackTrace();
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