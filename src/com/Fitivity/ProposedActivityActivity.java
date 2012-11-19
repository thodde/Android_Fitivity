package com.fitivity;

import java.util.ArrayList;
import java.util.List;

import com.fitivity.R;
import com.fitivity.PullToRefreshListView.OnRefreshListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProposedActivityActivity extends Activity {
	TextView title;
	TextView message;
	EditText commentText;
	Button comment;
	PullToRefreshListView commentList;
	ParseObject proposedActivity;
	ParseUser user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proposed_view);
		
		title = (TextView) findViewById(R.id.proposed_display_name);
		message = (TextView) findViewById(R.id.proposed_message_txt);
		commentText = (EditText) findViewById(R.id.commentText);
		comment = (Button) findViewById(R.id.commentButton);

		comment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (commentText.getText().toString() != "") {
					//ParseObject comment = new ParseObject("Comments");
					//ParseObject proposed = ParseObject.createWithoutData("ProposedActivity", getIntent().getStringExtra("ProposedActivityId"));
				}
			}
		});

		commentText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							commentText.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String activityID = bundle.getString("ProposedActivityId");
		
		ParseQuery query = new ParseQuery("ProposedActivity");
		query.getInBackground(activityID, new GetCallback() {
			public void done(ParseObject object, ParseException e) {
			    if (object != null) {
			    	proposedActivity = object;
			    	setActivityObject(proposedActivity);
			    }
			    else {
			    	e.printStackTrace();
			    }
			 }
		});
		
		title.setText("Proposed Activity");
		//message.setText(proposedActivity.getString("activityMessage"));
		findComments();
		
		commentList = (PullToRefreshListView) findViewById(R.id.commentList);

		// Set a listener to be invoked when the list should be refreshed.
		commentList.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				findComments();
			}
		});
	}
	
	public void setActivityObject(ParseObject currentObject) {
		proposedActivity = currentObject;
	}
	
	public void setUser(ParseUser currentUser) {
		user = currentUser;
	}

	public void findComments() {
		ParseQuery query = new ParseQuery("Comments");
		query.whereEqualTo("parent", proposedActivity);
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
						PlaceListAdapter adapter = new PlaceListAdapter(
								ProposedActivityActivity.this,
								R.layout.group_view, activities);
						commentList.setAdapter(adapter);
						commentList.onRefreshComplete();
					}
					else {
						commentList.onRefreshComplete();
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

			ParseUser user = activity.getParseUser("user");
			String type = activity.getString("message");
			String place = user.getUsername();

			description_text.setText(type);
			group_location_text.setText(place);

			return v;
		}
	}
}
