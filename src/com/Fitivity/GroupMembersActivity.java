package com.fitivity;

import java.util.ArrayList;
import java.util.List;

import com.fitivity.PullToRefreshListView.OnRefreshListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupMembersActivity extends Activity {
	
	PullToRefreshListView membersList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.members_view);
		
		membersList = (PullToRefreshListView) findViewById(R.id.membersList);
		
		// Set a listener to be invoked when the list should be refreshed.
		membersList.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
            	findActivities();
            }
		});
		
		findActivities();
	}
	
	public void findActivities() {
		
		ParseQuery query = new ParseQuery("GroupMembers");
		query.whereEqualTo("group", getIntent().getStringExtra("GroupId"));
		query.include("user");
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
    							GroupMembersActivity.this, R.layout.members_view,
    							activities);
    					membersList.setAdapter(adapter);
    					membersList.onRefreshComplete();
    				}
    				else {
    					
    					
    					membersList.onRefreshComplete();
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
			ParseUser user = activity.getParseUser("user");
			TextView description_text = (TextView) v.findViewById(R.id.description_text);
			//TextView group_location_text = (TextView) v.findViewById(R.id.group_location_text);
			
			description_text.setText(user.getUsername());

			return v;
		}
	}
}
