package com.fitivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fitivity.PullToRefreshListView.*;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class is for visiting a group page.
 * It can either be reached by creating a group or
 * by clicking on a group in the discover feed
 * @author Trevor Hodde
 */
public class GroupActivity extends Activity {

	TextView location;
	TextView activity;
	ImageView groupActivity;
	PullToRefreshListView proposedAcitivityList;
	Button membersButton;
	Button joinButton;
	Button mapButton;
	ParseObject date;
	boolean underDailyLimit;
	public ParseObject firstDate;
	public ParseObject secondDate;
	public String today;
	public String groupID;
	public ParseObject currentMember;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_view);
		
		//initialize the connection to parse
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");

		//grab references to the controls on the screen
		location = (TextView) findViewById(R.id.group_display_name);
		activity = (TextView) findViewById(R.id.group_activity_name);
		groupActivity = (ImageView) findViewById(R.id.group_activity);
		membersButton = (Button) findViewById(R.id.Button);
		joinButton = (Button) findViewById(R.id.group_join);
		mapButton = (Button) findViewById(R.id.group_map);

		location.setText(getIntent().getStringExtra("locationText"));
		activity.setText(getIntent().getStringExtra("activityText"));
		groupID = getIntent().getStringExtra("GroupId");
		
		//make sure the user is under the daily limit for proposed activites
		checkDailyCount();
		today = checkTodaysDate();
		
		//Quickly check to see if the user is already in the group or not
		ParseQuery query = new ParseQuery("GroupMembers");
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.whereEqualTo("group", groupID);
		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> groups, ParseException e) {
				if(e == null) {
					if(groups.size() != 0) {    //make sure there are actually items in the list
						if(groups.get(0) != null) {    //make sure the first item is real
							//changes the text if the user is in the list and grab a reference to them
							joinButton.setText("Unjoin");
							currentMember = groups.get(0);
						}
					}
				}
			}
		});
		
		joinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(joinButton.getText().equals("Join")) {
					try {
						/* Create the group member */
						ParseObject member = new ParseObject("GroupMembers");
						member.put("user", ParseUser.getCurrentUser());
						member.put("activity", getIntent().getStringExtra("activityText"));
						member.put("place", getIntent().getStringExtra("locationText"));
						member.put("group", groupID);
						member.save();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					joinButton.setText("Unjoin");
					AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(GroupActivity.this);
					dlgAlert.setMessage("You have joined the group!");
					dlgAlert.setTitle("Fitivity");
					dlgAlert.setPositiveButton("OK", null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();
				}
				else {
					/* Create the group member */
					currentMember.deleteInBackground();
					joinButton.setText("Join");
					AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(GroupActivity.this);
					dlgAlert.setMessage("You have left the group!");
					dlgAlert.setTitle("Fitivity");
					dlgAlert.setPositiveButton("OK", null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();
				}
			}
		});
		
		//click to view all group members
		membersButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(GroupActivity.this, GroupMembersActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("GroupId", getIntent().getStringExtra("GroupId"));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		//show the location of the group on the map
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//TODO: LOAD MAP LOCATION
			}
		});

		//click to propose a group activity
		groupActivity.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(firstDate != null && secondDate != null) {
					//grab the dates that the first two most recent objects were created at
					Date date1 = firstDate.getCreatedAt();
					Date date2 = secondDate.getCreatedAt();
					
					/**
					 * This is very gross looking but it goes as follow:
					 * 1. Check if today is a new day, if it is not...
					 * 2. Convert the two Dates to Strings
					 * 3. Attempt to grab the characters between 0 and 9 in the string
					 * 4. Make sure there are at least 10 characters available in the string
					 */
					if(date1 != null && date2 != null) {
						String strDate1 = date1.toString().substring(0, Math.min(date1.toString().length(), 9));
						String strDate2 = date2.toString().substring(0, Math.min(date2.toString().length(), 9));
						
						//if today does not match the last date, its a new day so we are under the limit
						//if the dates are equal, not allowed: return false
						if(!today.equals(strDate1)) {
							underDailyLimit = true;
						}
						else if(strDate1.equals(strDate2)) {
							underDailyLimit = false;
						}
						else {
							underDailyLimit = true;
						}
					}
					else {
						underDailyLimit = true;
					}
				}
				
				//if we are in fact under the daily limit for activities, open the group activity dialog box
				if(underDailyLimit) {
					final EditText input = new EditText(GroupActivity.this);
					input.setSingleLine(true);
					input.setHint("Describe the activity");
					AlertDialog.Builder ab = new AlertDialog.Builder(GroupActivity.this);
					ab.setTitle("Propose Group Activity").setView(input).setMessage("Are you sure you want to start a group activity?").setPositiveButton("Start",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									ParseQuery query = new ParseQuery("Groups");
									query.whereEqualTo("place", getIntent().getStringExtra("locationText"));
									query.whereEqualTo("activity",getIntent().getStringExtra("activityText"));
	
									ParseGeoPoint point = new ParseGeoPoint(
											getIntent().getDoubleExtra("latitude", 0),
											getIntent().getDoubleExtra("longitude", 0));
	
									query.whereWithinMiles("location",point, 0.1);
	
									try {
										ParseObject group = query.getFirst();
										String value = input.getText().toString();
										ParseObject groupActivity = new ParseObject("ProposedActivity");
										groupActivity.put("group", group);
										groupActivity.put("creator", ParseUser.getCurrentUser());
										groupActivity.put("activityMessage", value);
										groupActivity.put("postType", 1);
										groupActivity.put("activityCount", 0);
	
										ParseObject feedActivity = new ParseObject("ActivityEvent");
										feedActivity.put("group", group);
										feedActivity.put("creator", ParseUser.getCurrentUser());
										feedActivity.put("number", 1);
										feedActivity.put("postType", 1);
										feedActivity.put("proposedActivity", groupActivity);
										feedActivity.save();
										/*
										 * String channel = "Fitivity" +
										 * group.getObjectId();
										 * 
										 * ParsePush push = new ParsePush();
										 * push.setChannel(channel);
										 * push.setExpirationTimeInterval
										 * (86400);
										 * push.setPushToAndroid(true);
										 * String message = "" +
										 * ParseUser.getCurrentUser
										 * ().getUsername() +
										 * " proposed a group activity";
										 * push.setMessage(message);
										 * push.send();
										 */
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) { }
					}).show();
				}
				else {
					//If the user has already proposed two activities today...
					AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(GroupActivity.this);
					dlgAlert.setMessage("You can only propose an activity twice a day!");
					dlgAlert.setTitle("Limit Reached");
					dlgAlert.setPositiveButton("OK", null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();
				}
			}
		});

		proposedAcitivityList = (PullToRefreshListView) findViewById(R.id.proposedActivityList);

		proposedAcitivityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				onListItemClick(v, pos, id);
			}
		});

		// Set a listener to be invoked when the list should be refreshed.
		proposedAcitivityList.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				findActivities();
			}
		});
		
		findActivities();
	}
	
	/**
	 * Format the current date into something comparable to the date from Parse
	 * @return String the current date in a useable format
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
		ParseQuery query = new ParseQuery("ProposedActivity");
		query.orderByDescending("createdAt");
		query.whereEqualTo("creator", ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					LinkedList<ParseObject> activities = new LinkedList<ParseObject>();
					for (int i = 0; i < objects.size(); i++) {
						ParseObject activity = objects.get(i);
						activities.add(activity);
					}
					
					if(activities.size() < 2) {
						underDailyLimit = true;
					}
					else {
						firstDate = activities.get(0);
						secondDate = activities.get(1);
						
						//if they are null, the user has not yet created any activities today
						if (firstDate == null || secondDate == null) {
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
	
	/**
	 * If the user clicks on an activity, bring them to that page
	 * @param v View
	 * @param pos int position on list
	 * @param id long
	 */
	protected void onListItemClick(View v, int pos, long id) {
		ParseObject object = (ParseObject) proposedAcitivityList.getItemAtPosition(pos+1);
		Intent intent = new Intent();
		intent.setClass(GroupActivity.this, ProposedActivityActivity.class);
		
		Bundle bundle = new Bundle();
		bundle.putString("ProposedActivityId", object.getObjectId());

		intent.putExtras(bundle);
		startActivity(intent);
	}

	/**
	 * Grab all the activities that have been proposed in the current group
	 */
	public void findActivities() {
		ParseQuery innerQuery = new ParseQuery("Groups");
		innerQuery.whereEqualTo("place", getIntent().getStringExtra("locationText"));
		innerQuery.whereEqualTo("activity", getIntent().getStringExtra("activityText"));

		ParseGeoPoint point = new ParseGeoPoint(getIntent().getDoubleExtra("latitude", 0), getIntent().getDoubleExtra("longitude", 0));
		innerQuery.whereWithinMiles("location", point, 0.1);
		ParseQuery query = new ParseQuery("ProposedActivity");
		query.whereMatchesQuery("group", innerQuery);
		query.include("group");
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> activityList, ParseException e) {
				if (e == null) {
					ArrayList<ParseObject> activities = new ArrayList<ParseObject>();

					for (int i = 0; i < activityList.size(); i++) {
						ParseObject activity = activityList.get(i);
						activities.add(activity);
					}

					if (activities.size() > 0) {
						PlaceListAdapter adapter = new PlaceListAdapter(GroupActivity.this, R.layout.group_view, activities);
						proposedAcitivityList.setAdapter(adapter);
						proposedAcitivityList.onRefreshComplete();
					}
					else {
						proposedAcitivityList.onRefreshComplete();
					}
				}
				else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});

	}

	private class PlaceListAdapter extends ArrayAdapter<ParseObject> {
		private ArrayList<ParseObject> activities;

		public PlaceListAdapter(Context context, int textViewResourceId,
				ArrayList<ParseObject> items) {
			super(context, textViewResourceId, items);
			this.activities = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.feed_item_layout, null);
			}

			ParseObject activity = activities.get(position);
			TextView description_text = (TextView) v.findViewById(R.id.description_text);
			TextView group_location_text = (TextView) v.findViewById(R.id.group_location_text);
			ImageView picture = (ImageView) v.findViewById(R.id.feed_cell_picture);
			picture.setImageResource(R.drawable.feed_cell_icon_image);

			ParseObject group = activity.getParseObject("group");

			String type = activity.getString("activityMessage");
			String place = group.getString("place");

			description_text.setText(type);
			group_location_text.setText(place);

			return v;
		}
	}
}
