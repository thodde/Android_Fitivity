package com.fitivity;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.fitivity.PullToRefreshListView.OnRefreshListener;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ProfileActivity extends Activity {
	
	ImageView settings;
	PullToRefreshListView groupList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_view);
		
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");
		
		settings = (ImageView) findViewById(R.id.settings);

		TextView txtView = (TextView) findViewById(R.id.txt_display_tab);
		txtView.setText(ParseUser.getCurrentUser().getUsername());
		
		
		settings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				Intent mainIntent = new Intent(ProfileActivity.this,
                        SettingsActivity.class);
                ProfileActivity.this.startActivity(mainIntent);
			}
			
		});
		
		
		groupList = (PullToRefreshListView) findViewById(R.id.groupList);
	
		
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
		//query.include("group");
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback() {
    	    public void done(List<ParseObject> activityList, ParseException e) {
    	        if (e == null) {
    	            Log.d("groups", "Retrieved " + activityList.size() + " groups");
    	            
    	           
    	            
    	            ArrayList<ParseObject> activities = new ArrayList<ParseObject>();
    	            
    	            for (int i =0; i < activityList.size(); i++ ) {
    	            	ParseObject activity = activityList.get(i);
    	            	
    	            	activities.add(activity);
    	            	
    	            }
    	            
    	            
    	            if (activities.size() > 0) {
    					PlaceListAdapter adapter = new PlaceListAdapter(
    							ProfileActivity.this, R.layout.profile_view,
    							activities);
    					groupList.setAdapter(adapter);
    					groupList.onRefreshComplete();
    				}
    				else {
    					
    					
    					groupList.onRefreshComplete();
    				}
    	            
    	        } else {
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
			
			boolean customKeyExist = GenericStore.isCustomKeyExist(0, activity.getObjectId(), getApplicationContext());
			
			if (!customKeyExist) {
				Log.i("Store", "Key doesn't exist");
				//GenericStore.saveObject(0, activity.getObjectId(), activity.getUpdatedAt(), ProfileActivity.this);
			}
			else {
				Log.i("Store", "Key does exist");
				Date date = (Date) GenericStore.getObject(0, activity.getObjectId(), getApplicationContext());
				if (activity.getUpdatedAt().after(date)) {
					// New Alert
					ImageView indicator = (ImageView) v.findViewById(R.id.cell_indicator);
					indicator.setImageResource(android.R.drawable.star_big_on);
				}
				else {
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