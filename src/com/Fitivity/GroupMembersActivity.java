package com.fitivity;

import java.util.ArrayList;
import java.util.List;

import com.fitivity.R;
import com.fitivity.PullToRefreshListView.*;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupMembersActivity extends Activity {

	PullToRefreshListView membersList;
	ImageView picture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.members_view);

		membersList = (PullToRefreshListView) findViewById(R.id.membersList);
		
		//set listener to the pull to refresh handler
		membersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				onListItemClick(v, pos, id);
			}
		});
		
		// Set a listener to be invoked when the list should be refreshed.
		membersList.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				findMembers();
			}
		});

		findMembers();
	}

	public void findMembers() {
		ParseQuery query = new ParseQuery("GroupMembers");
		query.whereEqualTo("group", getIntent().getStringExtra("GroupId"));
		query.include("user");
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> activityList, ParseException e) {
				if (e == null) {
					ArrayList<ParseObject> activities = new ArrayList<ParseObject>();

					for (int i = 0; i < activityList.size(); i++) {
						ParseObject activity = activityList.get(i);
						activities.add(activity);
					}

					if (activities.size() > 0) {
						PlaceListAdapter adapter = new PlaceListAdapter(GroupMembersActivity.this, R.layout.members_view, activities);
						membersList.setAdapter(adapter);
						membersList.onRefreshComplete();
					}
					else {
						membersList.onRefreshComplete();
					}
				}
				else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}
	
	protected void onListItemClick(View v, int pos, long id) {
		ParseObject object = (ParseObject) membersList.getItemAtPosition(pos+1);
		ParseUser user = object.getParseUser("user");
		Intent intent = new Intent(GroupMembersActivity.this, GenericProfileActivity.class);

		Bundle bundle = new Bundle();
		bundle.putString("user", user.getUsername());
		bundle.putString("userID", user.getObjectId());

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
			ParseUser user = activity.getParseUser("user");
			TextView description_text = (TextView) v.findViewById(R.id.description_text);
			description_text.setText(user.getUsername());

			picture = (ImageView) v.findViewById(R.id.feed_cell_picture);
			
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

			return v;
		}
	}
}
