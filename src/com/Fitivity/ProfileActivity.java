package com.fitivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fitivity.R;
import com.fitivity.PullToRefreshListView.*;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileActivity extends Activity {

	ImageView settings;
	ImageView profilePic;
	String information = "";
	String description = "";
	PullToRefreshListView groupList;
	ParseUser user;
	Bundle bundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_view);

		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");

		settings = (ImageView) findViewById(R.id.settings);
		profilePic = (ImageView) findViewById(R.id.profilePicture);
		groupList = (PullToRefreshListView) findViewById(R.id.groupList);

		TextView txtView = (TextView) findViewById(R.id.txt_display_tab);
		txtView.setText(ParseUser.getCurrentUser().getUsername());

		user = ParseUser.getCurrentUser();
		
		ParseFile profileData = (ParseFile) user.get("image");
		profileData.getDataInBackground(new GetDataCallback() {
			public void done(byte[] data, ParseException e) {
				if (e == null) {
					// data has the bytes for the profilePicture
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					profilePic.setImageBitmap(bitmap);
				}
				else {
					// something went wrong
					profilePic.setImageResource(R.drawable.feed_cell_profile_placeholder);
				}
			}
		});

		settings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent mainIntent = new Intent(ProfileActivity.this, SettingsActivity.class);
				ProfileActivity.this.startActivity(mainIntent);
			}
		});
		
		//set listener to the pull to refresh handler
		groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				onListItemClick(v, pos, id);
			}
		});

		// Set a listener to be invoked when the list should be refreshed.
		groupList.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				findActivities();
			}
		});
		
		findActivities();
	}
	
	public void findActivities() {
		ParseQuery query = new ParseQuery("GroupMembers");
		query.orderByDescending("updatedAt");
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> activityList, ParseException e) {
				if (e == null) {
					ArrayList<ParseObject> activities = new ArrayList<ParseObject>();

					for (int i = 0; i < activityList.size(); i++) {
						ParseObject activity = activityList.get(i);
						activities.add(activity);
					}

					if (activities.size() > 0) {
						PlaceListAdapter adapter = new PlaceListAdapter(ProfileActivity.this, R.layout.profile_view, activities);
						groupList.setAdapter(adapter);
						groupList.onRefreshComplete();
					}
					else {
						groupList.onRefreshComplete();
					}
				}
				else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}
	
	protected void onListItemClick(View v, int pos, long id) {
		ParseObject object = (ParseObject) groupList.getItemAtPosition(pos+1);
		Intent intent = new Intent();
		intent.setClass(ProfileActivity.this, GroupActivity.class);

		String group = object.getString("activity");
		String place = object.getString("place");
		Bundle bundle = new Bundle();
		bundle.putString("activityText", group);
		bundle.putString("locationText", place);

		intent.putExtras(bundle);
		startActivity(intent);
	}

	private class PlaceListAdapter extends ArrayAdapter<ParseObject> {
		private ArrayList<ParseObject> activities;

		public PlaceListAdapter(Context context, int textViewResourceId, ArrayList<ParseObject> items) {
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

			boolean customKeyExist = GenericStore.isCustomKeyExist(0, activity.getObjectId(), getApplicationContext());

			if (!customKeyExist) {
				Log.i("Store", "Key doesn't exist");
				// GenericStore.saveObject(0, activity.getObjectId(),
				// activity.getUpdatedAt(), ProfileActivity.this);
			} else {
				Log.i("Store", "Key does exist");
				Date date = (Date) GenericStore.getObject(0, activity.getObjectId(), getApplicationContext());
				if (activity.getUpdatedAt().after(date)) {
					// New Alert
					ImageView indicator = (ImageView) v.findViewById(R.id.cell_indicator);
					indicator.setImageResource(android.R.drawable.star_big_on);
				} else {
					ImageView indicator = (ImageView) v.findViewById(R.id.cell_indicator);
					indicator.setImageResource(R.drawable.feed_cell_arrow);
				}
			}

			TextView description_text = (TextView) v.findViewById(R.id.description_text);
			TextView group_location_text = (TextView) v.findViewById(R.id.group_location_text);
			ImageView picture = (ImageView) v.findViewById(R.id.feed_cell_picture);
			picture.setImageResource(R.drawable.feed_cell_icon_image);

			String type = activity.getString("activity");
			String place = activity.getString("place");

			description_text.setText(type);
			group_location_text.setText(place);

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