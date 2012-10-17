package com.fitivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fitivity.R;
import com.fitivity.PullToRefreshListView.OnRefreshListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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

public class GroupActivity extends Activity {

	TextView location;
	TextView activity;
	ImageView groupActivity;
	PullToRefreshListView proposedAcitivityList;
	Button membersButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_view);

		location = (TextView) findViewById(R.id.group_display_name);
		activity = (TextView) findViewById(R.id.group_activity_name);
		groupActivity = (ImageView) findViewById(R.id.group_activity);
		membersButton = (Button) findViewById(R.id.Button);

		location.setText(getIntent().getStringExtra("locationText"));
		activity.setText(getIntent().getStringExtra("activityText"));

		membersButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Intent intent = new Intent();

				intent.setClass(GroupActivity.this, GroupMembersActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("GroupId",
						getIntent().getStringExtra("GroupId"));

				intent.putExtras(bundle);
				startActivity(intent);

			}
		});

		groupActivity.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				final EditText input = new EditText(GroupActivity.this);
				input.setSingleLine(true);
				input.setHint("Describe the activity");

				AlertDialog.Builder ab = new AlertDialog.Builder(
						GroupActivity.this);
				ab.setTitle("Propose Group Activity")
						.setView(input)
						.setMessage(
								"Are you sure you want to start a group activity?")
						.setPositiveButton("Start",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// continue

										ParseQuery query = new ParseQuery(
												"Groups");

										query.whereEqualTo("place", getIntent()
												.getStringExtra("locationText"));
										query.whereEqualTo(
												"activity",
												getIntent().getStringExtra(
														"activityText"));

										ParseGeoPoint point = new ParseGeoPoint(
												getIntent().getDoubleExtra(
														"latitude", 0),
												getIntent().getDoubleExtra(
														"longitude", 0));

										query.whereWithinMiles("location",
												point, 0.1);

										try {
											ParseObject group = query
													.getFirst();
											String value = input.getText()
													.toString();
											ParseObject groupActivity = new ParseObject(
													"ProposedActivity");
											groupActivity.put("group", group);
											groupActivity.put("creator",
													ParseUser.getCurrentUser());
											groupActivity.put(
													"activityMessage", value);

											// message

											ParseObject feedActivity = new ParseObject(
													"ActivityEvent");
											feedActivity.put("group", group);
											feedActivity.put("creator",
													ParseUser.getCurrentUser());
											feedActivity.put("type", "GROUP");
											feedActivity.put("status", "N/A");
											feedActivity.put("number", 1);
											feedActivity.put(
													"proposedActivity",
													groupActivity);
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
										} catch (ParseException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing
									}
								}).show();

			}

		});

		proposedAcitivityList = (PullToRefreshListView) findViewById(R.id.proposedActivityList);

		proposedAcitivityList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> av, View v, int pos,
							long id) {
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

	protected void onListItemClick(View v, int pos, long id) {
		ParseObject object = (ParseObject) proposedAcitivityList
				.getItemAtPosition(pos);
		Intent intent = new Intent();

		intent.setClass(GroupActivity.this, ProposedActivityActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("ProposedActivityId", object.getObjectId());

		intent.putExtras(bundle);
		startActivity(intent);

	}

	public void findActivities() {

		ParseQuery innerQuery = new ParseQuery("Groups");

		innerQuery.whereEqualTo("place",
				getIntent().getStringExtra("locationText"));
		innerQuery.whereEqualTo("activity",
				getIntent().getStringExtra("activityText"));

		ParseGeoPoint point = new ParseGeoPoint(getIntent().getDoubleExtra(
				"latitude", 0), getIntent().getDoubleExtra("longitude", 0));

		innerQuery.whereWithinMiles("location", point, 0.1);

		ParseQuery query = new ParseQuery("ProposedActivity");
		query.whereMatchesQuery("group", innerQuery);
		query.include("group");
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> activityList, ParseException e) {
				if (e == null) {
					Log.d("groups", "Retrieved " + activityList.size()
							+ " groups");

					ArrayList<ParseObject> activities = new ArrayList<ParseObject>();

					for (int i = 0; i < activityList.size(); i++) {
						ParseObject activity = activityList.get(i);

						activities.add(activity);
					}

					if (activities.size() > 0) {
						PlaceListAdapter adapter = new PlaceListAdapter(
								GroupActivity.this, R.layout.group_view,
								activities);
						proposedAcitivityList.setAdapter(adapter);
						proposedAcitivityList.onRefreshComplete();
					} else {

						proposedAcitivityList.onRefreshComplete();
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
			TextView description_text = (TextView) v
					.findViewById(R.id.description_text);
			TextView group_location_text = (TextView) v
					.findViewById(R.id.group_location_text);
			ImageView picture = (ImageView) v
					.findViewById(R.id.feed_cell_picture);
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
