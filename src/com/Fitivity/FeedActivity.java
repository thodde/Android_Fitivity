/**
 * Author: Trevor Hodde
 * 
 * This class controls the discover feed.
 * 
 * (c) fitivity 2012
 */

package com.fitivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fitivity.R;
import com.fitivity.PullToRefreshListView.*;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class creates and populates the Discover Feed
 * by pulling the proper user data from Parse and
 * creating cells for each activity.
 */
public class FeedActivity extends Activity {
	public final int cellTypeGroup = 0;
	public final int cellTypePA = 1;
	public final int cellTypeComment = 2;
	PullToRefreshListView refreshList;
	ImageButton sharingButton;
	ImageButton sortButton;
	String information = "";
	String description = "";
	ImageView picture;
	ImageView todayLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_view);

		//initialize the connection to parse
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");

		//subscribe the device to push notifications
		PushService.subscribe(this, "Fitivity", FeedActivity.class);

		//grab handles to the needed controls
		refreshList = (PullToRefreshListView) findViewById(R.id.refreshList);
		sharingButton = (ImageButton) findViewById(R.id.shareButton);
		todayLabel = (ImageView) findViewById(R.id.cell_indicator);
		sortButton = (ImageButton) findViewById(R.id.sortButton);
		
		//set listener to the pull to refresh handler
		refreshList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				onListItemClick(v, pos, id);
			}
		});

		// Set a listener to be invoked when the list should be refreshed.
		refreshList.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				findActivities();
			}
		});

		// Set a listener on the sharing button so we can tell when its been clicked
		sharingButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				displayShareOptions();
			}
		});
		
		sortButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sortFeed();
			}
		});

		//grab activities from Parse
		findActivities();
	}
	
	/**
	 * This method allows the user to share the app with other users via SMS,
	 * Email, Facebook, or Twitter
	 */
	public void displayShareOptions() {
		String shareStr = "Join our fitivity community to get active with myself and other people "
				+ "interested in pick-up sports, fitness, running, or recreation. You can download "
				+ "it for free in the Apple App Store or in Google Play.";

		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("plain/text");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareStr);
		startActivity(Intent.createChooser(sharingIntent, "Share using"));
	}

	/**
	 * Grab activities from the parse database and populate the cells in the 
	 * feed with all the important data (username, location, activity, group, etc.)
	 */
	public void findActivities() {
		//Only grab items within 25 miles of the current location
		ParseGeoPoint point = new ParseGeoPoint();
		ParseQuery innerQuery = new ParseQuery("Groups");
		innerQuery.whereWithinMiles("location", point, 25);

		//Populate the cells with activities
		ParseQuery query = new ParseQuery("ActivityEvent");
		query.setLimit(50);
		query.orderByDescending("updatedAt");
		query.include("creator");
		query.include("group");
		query.include("comment");
		query.include("proposedActivity");
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> activityList, ParseException e) {
				if (e == null) {
					ArrayList<ParseObject> activities = new ArrayList<ParseObject>();
					for (int i = 0; i < activityList.size(); i++) {
						ParseObject activity = activityList.get(i);
						activities.add(activity);
					}

					if (activities.size() > 0) {
						PlaceListAdapter adapter = new PlaceListAdapter(
								FeedActivity.this, R.layout.feed_view,
								activities);
						refreshList.setAdapter(adapter);
						refreshList.onRefreshComplete();
					} 
					else {
						ParseObject activity = new ParseObject("ActivityEvent");
						activities.add(activity);
						PlaceListAdapter adapter = new PlaceListAdapter(
								FeedActivity.this, R.layout.feed_view,
								activities);
						refreshList.setAdapter(adapter);
						refreshList.onRefreshComplete();
					}
				}
				else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}
	
	public void sortFeed() {
		//TODO: Handle sorting the discover feed
	}

	protected void onListItemClick(View v, int pos, long id) {
		ParseObject object = (ParseObject) refreshList.getItemAtPosition(pos+1);
		Intent intent = new Intent();
		int type = object.getInt("postType");
		Bundle bundle = new Bundle();
		
		if(type == 0) {
			intent.setClass(FeedActivity.this, GroupActivity.class);
			
			ParseObject group = object.getParseObject("group");
			bundle.putString("activityText", group.getString("activity"));
			bundle.putString("locationText", group.getString("place"));
			bundle.putString("GroupId", group.getObjectId());
			ParseGeoPoint point = group.getParseGeoPoint("location");
			bundle.putDouble("latitude", point.getLatitude());
			bundle.putDouble("longitude", point.getLongitude());
		}
		else if(type == 1) {
			intent.setClass(FeedActivity.this, ProposedActivityActivity.class);
			ParseObject activityID = object.getParseObject("proposedActivity");

			bundle.putString("message", activityID.getString("activityMessage"));
			bundle.putString("ActivityId", activityID.getObjectId());
		}
		else {
			intent.setClass(FeedActivity.this, ProposedActivityActivity.class);
			ParseObject activityID = object.getParseObject("proposedActivity");
			
			bundle.putString("message", activityID.getString("activityMessage"));
			bundle.putString("ActivityId", activityID.getObjectId());
		}

		intent.putExtras(bundle);
		startActivity(intent);
	}

	private class PlaceListAdapter extends ArrayAdapter<ParseObject> {
		private ArrayList<ParseObject> activities;
		private int numberOfMembers = 1;

		public PlaceListAdapter(Context context, int textViewResourceId, ArrayList<ParseObject> items) {
			super(context, textViewResourceId, items);
			this.activities = items;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.feed_item_layout, null);
			}

			ParseObject activity = activities.get(position);

			ImageView todayLabel = (ImageView) v.findViewById(R.id.cell_indicator);
			TextView description_text = (TextView) v.findViewById(R.id.description_text);
			TextView group_location_text = (TextView) v.findViewById(R.id.group_location_text);
			picture = (ImageView) v.findViewById(R.id.feed_cell_picture);
			picture.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					ParseObject object = (ParseObject) refreshList.getItemAtPosition(position+1);
					ParseUser user = object.getParseUser("creator");
					visitProfile(user);
				}
			});

			ParseUser user = activity.getParseUser("creator");
			int type = activity.getInt("postType");
			ParseObject pa = activity.getParseObject("proposedActivity");
			
			//Catch glitch in parse storage for now
			//make sure that proposed activities are picked up
			if((pa != null && type == 2) || (pa != null && type == 0)) {
				type = 1;
			}

			if (type == cellTypeGroup) {
				numberOfMembers = activity.getInt("number");

				if (numberOfMembers > 1) {
					description = "" + numberOfMembers + " people are doing";
					picture.setImageResource(R.drawable.group_icon_large);
				}
				else {
					description = "" + user.getUsername() + " is doing";
					try {
						ParseFile profileData = (ParseFile) user.get("image");
						if(profileData != null) {
							profileData.getDataInBackground(new GetDataCallback() {
								public void done(byte[] data, ParseException e) {
									if (e == null) {
										// data has the bytes for the profilePicture
										Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
										picture.setImageBitmap(bitmap);
									}
									else {
										// something went wrong
										picture.setImageResource(R.drawable.feed_cell_profile_placeholder);
									}
								}
							});
						}
						else {
							picture.setImageResource(R.drawable.feed_cell_profile_placeholder);
						}
					}
					catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}
			else if(type == cellTypePA) {
				picture.setImageResource(R.drawable.activity_icon_large);
				description = "" + user.getUsername() + " proposed an activity";
			}

			description_text.setText(description);

			ParseObject group = activity.getParseObject("group");
			information = group.getString("activity") + " at " + group.getString("place");

			group_location_text.setText(information);
			
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
			String dateToday = "" + weekday + " " + monthOfYear + " " + dayOfMonth;
		    
		    //store the date and time that the activity was created
		    Date parseDate = activity.getCreatedAt();
		    String strParseDate = parseDate.toString().substring(0, Math.min(parseDate.toString().length(), 10));
		    
		    //if the dates are equal, display the today label
			if (strParseDate.equals(dateToday)) {
				//set today label to visible
				todayLabel.setVisibility(View.VISIBLE);
			}
			else {
				todayLabel.setVisibility(View.INVISIBLE);
			}
			
			return v;
		}
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
	 * If we choose to view a users profile from the discover feed,
	 * we need a reference to the users name and ID so we can
	 * pull their page later
	 * @param user ParseUser
	 */
	public void visitProfile(ParseUser user) {
		Intent intent = new Intent();
		intent.setClass(FeedActivity.this, GenericProfileActivity.class);
		
		//stick the needed info into the bundle for later
		Bundle bundle = new Bundle();
		bundle.putString("user", user.getUsername());
		bundle.putString("userID", user.getObjectId());

		intent.putExtras(bundle);
		
		startActivity(intent);
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