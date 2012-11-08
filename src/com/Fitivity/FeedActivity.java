/**
 * Author: Trevor Hodde
 * 
 * This class controls the discover feed.
 * 
 * (c) fitivity 2012
 */

package com.fitivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
	String information = "";
	String description = "";
	ImageView picture;
	ImageView todayLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_view);

		//initialize the connection to parse
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq",
				"krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");

		//subscribe the device to push notifications
		PushService.subscribe(this, "Fitivity", FeedActivity.class);

		//grab handles to the needed controls
		refreshList = (PullToRefreshListView) findViewById(R.id.refreshList);
		sharingButton = (ImageButton) findViewById(R.id.shareButton);
		todayLabel = (ImageView) findViewById(R.id.cell_indicator);

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

	protected void onListItemClick(View v, int pos, long id) {
		ParseObject object = (ParseObject) refreshList.getItemAtPosition(pos+1);
		Intent intent = new Intent();
		int type = object.getInt("postType");
		Bundle bundle = new Bundle();
		intent.setClass(FeedActivity.this, GroupActivity.class);
		
		ParseObject group = object.getParseObject("group");
		bundle.putString("activityText", group.getString("activity"));
		bundle.putString("locationText", group.getString("place"));
		bundle.putString("GroupId", group.getObjectId());
		ParseGeoPoint point = group.getParseGeoPoint("location");
		bundle.putDouble("latitude", point.getLatitude());
		bundle.putDouble("longitude", point.getLongitude());

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

			todayLabel = (ImageView) findViewById(R.id.cell_indicator);
			
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
			if(pa != null && type == 0) {
				type = 1;
			}

			if (type == cellTypeGroup) {
				numberOfMembers = activity.getInt("number");

				if (numberOfMembers > 1) {
					description = "This group now has " + numberOfMembers + " members.";
					picture.setImageResource(R.drawable.group_icon_large);
				}
				else {
					description = "" + user.getUsername() + " created a Group";
					try {
						ParseFile profileData = (ParseFile) user.get("image");
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
					catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}
			else if(type == cellTypePA) {
				picture.setImageResource(R.drawable.activity_icon_large);
				description = "" + user.getUsername() + " proposed a group activity";
			}

			description_text.setText(description);

			ParseObject group = activity.getParseObject("group");
			information = group.getString("activity") + " @ "
					+ group.getString("place");

			if (type == cellTypeComment) {
				// ParseObject comment = activity.getParseObject("comment");
				// String message = comment.getString("message");
				// description = "" + user.getUsername() + "made a comment";
				// description_text.setText(description);
				// group_location_text.setText(message);
			}

			group_location_text.setText(information);
			
			//store the current date and time
			Calendar currentDate = Calendar.getInstance();
			String weekday = getDayOfWeek(Calendar.DAY_OF_WEEK);
			String month = getMonthOfYear(Calendar.MONTH);
			String dateToday = "" + (weekday + ", " + (Calendar.DAY_OF_MONTH) + " " + month + " " + (Calendar.YEAR) + " " + (Calendar.HOUR_OF_DAY) + ":" + (Calendar.MINUTE) + ":" + (Calendar.SECOND));
		    
		    //store the date and time that the activity was created
		    Date parseDate = activity.getDate("createdAt");
		    
		    //TODO: TEST THIS DATE STUFF
		    //make sure the day, month, and year are all the same
			//if (parseDate.toString().equals(dateToday)) {
				//set today label to visible
			//	todayLabel.setVisibility(ImageView.VISIBLE);
			//}
			
			return v;
		}
	}
	
	private String getDayOfWeek(int day) {
		String strDay;
		
		switch(day) {
		case 0:
			strDay = "Sun";
		case 1:
			strDay = "Mon";
		case 2:
			strDay = "Tue";
		case 3:
			strDay = "Wed";
		case 4:
			strDay = "Thu";
		case 5:
			strDay = "Fri";
		case 6:
			strDay = "Sat";
		default: 
			strDay = "";
		}
		
		return strDay;
	}
	
	private String getMonthOfYear(int month) {
		String strMonth;
		
		switch(month) {
		case 0:
			strMonth = "Jan";
		case 1:
			strMonth = "Feb";
		case 2:
			strMonth = "Mar";
		case 3:
			strMonth = "Apr";
		case 4:
			strMonth = "May";
		case 5:
			strMonth = "Jun";
		case 6:
			strMonth = "Jul";
		case 7:
			strMonth = "Aug";
		case 8:
			strMonth = "Sep";
		case 9:
			strMonth = "Oct";
		case 10:
			strMonth = "Nov";
		case 11:
			strMonth = "Dec";
		default: 
			strMonth = "";
		}
		
		return strMonth;
	}
	
	public void visitProfile(ParseUser user) {
		Intent intent = new Intent();
		intent.setClass(FeedActivity.this, GenericProfileActivity.class);
		
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