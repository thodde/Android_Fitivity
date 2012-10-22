/**
 * Author: Trevor Hodde
 * 
 * This class controls the discover feed.
 * 
 * (c) fitivity 2012
 */

package com.fitivity;

import java.util.ArrayList;
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
import android.app.AlertDialog;
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
	private final int cellTypeGroup = 0;
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

		// Set a listener on the sharing button so we can tell when its been
		// clicked
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
					Log.d("score", "Retrieved " + activityList.size() + " activities");

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
		ParseObject object = (ParseObject) refreshList.getItemAtPosition(pos);
		Intent intent = new Intent();
		int type = object.getInt("postType");

		if (type == cellTypeGroup) {
			ParseObject proposed = object.getParseObject("proposedActivity");
			intent.setClass(FeedActivity.this, ProposedActivityActivity.class);
			Bundle bundle = new Bundle();
			// bundle.putString("ProposedActivityId", proposed.getObjectId());

			intent.putExtras(bundle);
			startActivity(intent);
		}
		else {
			intent.setClass(FeedActivity.this, GroupActivity.class);

			ParseObject group = object.getParseObject("group");
			Bundle bundle = new Bundle();
			bundle.putString("activityText", group.getString("activity"));
			bundle.putString("locationText", group.getString("place"));
			bundle.putString("GroupId", group.getObjectId());
			ParseGeoPoint point = group.getParseGeoPoint("location");
			bundle.putDouble("latitude", point.getLatitude());
			bundle.putDouble("longitude", point.getLongitude());

			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	private class PlaceListAdapter extends ArrayAdapter<ParseObject> {
		private ArrayList<ParseObject> activities;
		private final int cellTypeGroup = 0;
		private final int cellTypePA = 1;
		private final int cellTypeComment = 2;
		private int numberOfMembers = 1;

		public PlaceListAdapter(Context context, int textViewResourceId,
				ArrayList<ParseObject> items) {
			super(context, textViewResourceId, items);
			this.activities = items;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.feed_item_layout, null);
			}

			ParseObject activity = activities.get(position);

			TextView description_text = (TextView) v
					.findViewById(R.id.description_text);
			TextView group_location_text = (TextView) v
					.findViewById(R.id.group_location_text);
			picture = (ImageView) v.findViewById(R.id.feed_cell_picture);
			picture.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					ParseObject object = (ParseObject) refreshList
							.getItemAtPosition(position);
					Intent intent = new Intent();
					ParseUser user = object.getParseUser("creator");
					AlertDialog.Builder ab = new AlertDialog.Builder(
							FeedActivity.this);
					ab.setTitle("User").setMessage(user.getUsername()).show();
				}
			});

			ParseUser user = activity.getParseUser("creator");
			int type = activity.getInt("postType");

			if (type == cellTypeGroup) {
				numberOfMembers = activity.getInt("number");

				if (numberOfMembers > 1) {
					description = "This group now has " + numberOfMembers
							+ " members.";
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
									Bitmap bitmap = BitmapFactory
											.decodeByteArray(data, 0,
													data.length);
									picture.setImageBitmap(bitmap);
								}
								else {
									// something went wrong
									picture.setImageResource(R.drawable.feed_cell_profile_placeholder);
								}
							}
						});
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}

			if (type == cellTypePA) {
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
		    Date date = new Date();
		    String[] dateResult = date.toString().split("\\s");
		    
		    //store the date and time that the activity was created
		    Date parseDate = activity.getDate("createdAt");
		    String[] createdAtResult = parseDate.toString().split("\\s"); 
		    
		    //TODO: TEST THIS DATE STUFF
		    //make sure the day, month, and year are all the same
			if((dateResult[2].equals(createdAtResult[1])) && (dateResult[1].equals(createdAtResult[2]))) {
				todayLabel.setVisibility(ImageView.VISIBLE);
			}
			else {
				todayLabel.setVisibility(ImageView.GONE);
			}
			
			return v;
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