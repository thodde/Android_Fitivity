package com.Fitivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.Fitivity.PullToRefreshListView.*;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class FeedActivity extends Activity {
	
	PullToRefreshListView refreshList;
	String information = "";
	String description = "";
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_view);	
		
		 Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl"); 
		 
		 PushService.subscribe(this, "", FeedActivity.class);
		 refreshList = (PullToRefreshListView) findViewById(R.id.refreshList);
		 
		 refreshList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
					onListItemClick(v, pos, id);
				}

			});
		 
		 
		// Set a listener to be invoked when the list should be refreshed.
			refreshList.setOnRefreshListener(new OnRefreshListener() {
	            @Override
	            public void onRefresh() {
	            	findActivities();
	            }
			});
	        
			findActivities();
		}
		
		public void findActivities() {
			
			ParseGeoPoint point = new ParseGeoPoint(); 
			ParseQuery innerQuery = new ParseQuery("Groups");
			innerQuery.whereWithinMiles("location", point, 50);
			
			ParseQuery query = new ParseQuery("ActivityEvent");
			//query.whereMatchesQuery("group", innerQuery);
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
	    	            
	    	            for (int i =0; i < activityList.size(); i++ ) {
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
	    					//activity.FitivityFeedEntryItemActivityName = "No activities found";
	    					activities.add(activity);
	    					PlaceListAdapter adapter = new PlaceListAdapter(
	    							FeedActivity.this, R.layout.feed_view,
	    							activities);
	    					refreshList.setAdapter(adapter);
	    					refreshList.onRefreshComplete();
	    				}
	    	            
	    	        } else {
	    	            Log.d("score", "Error: " + e.getMessage());
	    	        }
	    	    }
	    	});
			
		}
		
		protected void onListItemClick(View v, int pos, long id) {
			ParseObject object = (ParseObject)refreshList.getItemAtPosition(pos);
			Intent intent = new Intent();
			String status = object.getString("status");
			String type = object.getString("type");
			if (status.contentEquals("COMMENT") || type.contentEquals("GROUP")) {
				
			    ParseObject proposed = object.getParseObject("proposedActivity");
				intent.setClass(FeedActivity.this, ProposedActivityActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("ProposedActivityId", proposed.getObjectId());
				
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
		
		// Displays list of places to check into 
		private class PlaceListAdapter extends ArrayAdapter<ParseObject> {
			private ArrayList<ParseObject> activities;

			public PlaceListAdapter(Context context, int textViewResourceId,
					ArrayList<ParseObject> items) {
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
				
				TextView description_text = (TextView) v.findViewById(R.id.description_text);
				TextView group_location_text = (TextView) v.findViewById(R.id.group_location_text);
				ImageView picture = (ImageView) v.findViewById(R.id.feed_cell_picture);
				picture.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View view) {
		            	ParseObject object = (ParseObject)refreshList.getItemAtPosition(position);
		    			Intent intent = new Intent();
						
							ParseUser user = object.getParseUser("creator");
							AlertDialog.Builder ab = new AlertDialog.Builder(FeedActivity.this);
						    ab.setTitle("User")
						    .setMessage(user.getUsername())
						    .show();
		            }
		        });
			
				
				ParseUser user = activity.getParseUser("creator");
				
				String type = activity.getString("type");	
				String status = activity.getString("status");
				
				
				
				if (status.contentEquals("NEW") && type.contentEquals("NORMAL")) {
					description = "" + user.getUsername() + " created a Group";
					picture.setImageResource(R.drawable.feed_cell_profile_placeholder);
				}
				else if (status.contentEquals("OLD") && type.contentEquals("NORMAL")) {
					description = "This group now has "	+ activity.getInt("number") + " members.";
					picture.setImageResource(R.drawable.feed_cell_icon_image);
				
				}
				
				if (type.contentEquals("GROUP")) {
					picture.setImageResource(R.drawable.feed_cell_profile_placeholder);
					description = "" + user.getUsername() + " proposed a group activity";
				}
				
				description_text.setText(description);
				
				
				
			 ParseObject group = activity.getParseObject("group");
				 information = group.getString("activity") + " @ " + group.getString("place");
				
				group_location_text.setText(information);
				
				if (status.contentEquals("COMMENT")) {
					
					ParseObject comment = activity.getParseObject("comment");
					String message = comment.getString("message");
					description = "" + user.getUsername() + "made a comment";
					description_text.setText(description);
					group_location_text.setText(message);
					
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