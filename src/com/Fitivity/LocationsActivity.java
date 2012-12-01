package com.fitivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.fitivity.R;
import com.parse.Parse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationsActivity extends Activity implements LocationListener {

	private static final String API_URL = "https://maps.googleapis.com/maps/api/place/search/json?";

	LocationManager mlocManager;
	double currentLatitude;
	double currentLongitude;
	String locationName;
	String search = "";
	String provider;
	// My Google Maps API key: 0cFaOS5j44okdaIchN0i43JThF60jiSIUoRi_dw
	public static final String CLIENT_ID = "0cFaOS5j44okdaIchN0i43JThF60jiSIUoRi_dw";

	private ListView mListView;
	private NearbyAdapter mAdapter;
	private ArrayList<Place> mNearbyList;
	private ProgressDialog mProgress;
	private EditText filterText;
	private ImageButton addLocation;
	int requestCode = 123;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locations_layout);

		//initialize the connection to parse
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");
		
		mListView = (ListView) findViewById(R.id.lv_places);
		filterText = (EditText) findViewById(R.id.search_box);
		addLocation = (ImageButton) findViewById(R.id.add_location);

		//This button allows the user to add a location
		addLocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(LocationsActivity.this);
				dlgAlert.setMessage("Add a custom location feature coming soon!");
				dlgAlert.setTitle("Fitivity");
				dlgAlert.setPositiveButton("OK", null);
				dlgAlert.setCancelable(true);
				dlgAlert.create().show();
				
				//Intent intent = new Intent();
				//intent.setClass(LocationsActivity.this, MapViewActivity.class);
				//startActivityForResult(intent, requestCode);
			}
		});

		filterText.addTextChangedListener(filterTextWatcher);
		filterText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {

							InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									filterText.getWindowToken(), 0);

							return true;
						}
						return false;
					}
				});

		mAdapter = new NearbyAdapter(this);
		mNearbyList = new ArrayList<Place>();
		mProgress = new ProgressDialog(this);

		mProgress.setMessage("Loading data ...");
		mProgress.show();

		mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		provider = mlocManager.getBestProvider(criteria, true);

		mlocManager.requestLocationUpdates(provider, 0, 0, this);

		/* Making the ListView clickable */
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				onListItemClick(v, pos, id);
			}

		});
		getLocation();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	  if(resultCode == 123) {
		  //grab data from MapViewActivity
		  locationName = data.getStringExtra("locationName");
		  currentLatitude = data.getDoubleExtra("latitude", currentLatitude);
		  currentLongitude = data.getDoubleExtra("longitude", currentLongitude);
		  
		  //pass data to CreateActivityActivity
		  Intent resultIntent = new Intent();
		  resultIntent.putExtra("locationName", locationName);
		  resultIntent.putExtra("latitude", currentLatitude);
		  resultIntent.putExtra("longitude", currentLongitude);
		  setResult(Activity.RESULT_OK, resultIntent);
		  
		  finish();
	  }
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			search = s.toString();
			getLocation();
		}

	};

	/* This method starts new activity and passes along Crowd object from list */
	protected void onListItemClick(View v, int pos, long id) {
		Place p = (Place) mListView.getItemAtPosition(pos);

		Intent resultIntent = new Intent();

		resultIntent.putExtra("name", p.name);
		resultIntent.putExtra("latitude", p.location.getLatitude());
		resultIntent.putExtra("longitude", p.location.getLongitude());

		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	public ArrayList<Place> getNearby(double latitude, double longitude)
			throws Exception {
		ArrayList<Place> venueList = new ArrayList<Place>();

		try {
			String ll = String.valueOf(latitude) + ","
					+ String.valueOf(longitude);
			URL url = new URL(API_URL + "location=" + ll
					+ "&radius=5000&types=campground|gym|park&name=" + search
					+ "&sensor=true&key=" + CLIENT_ID);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);

			urlConnection.connect();

			String response = streamToString(urlConnection.getInputStream());
			JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();

			JSONArray venues = (JSONArray) jsonObj.getJSONArray("results");

			int length = venues.length();

			if (length > 0) {
				Location whereAmI = mlocManager.getLastKnownLocation(provider);
				for (int j = 0; j < length; j++) {
					// final double CONVERT = 0.000621371192;

					JSONObject item = (JSONObject) venues.get(j);

					Place venue = new Place();

					venue.id = item.getString("id");
					venue.name = item.getString("name");

					JSONObject location = (JSONObject) item.getJSONObject(
							"geometry").getJSONObject("location");

					Location loc = new Location(
							LocationManager.NETWORK_PROVIDER);

					loc.setLatitude(Double.valueOf(location.getDouble("lat")));
					loc.setLongitude(Double.valueOf(location.getDouble("lng")));

					venue.location = loc;
					// venue.address = "address";
					// //location.getString("address");

					DecimalFormat f = new DecimalFormat();
					f.setMaximumFractionDigits(2);
					double distance = whereAmI.distanceTo(loc);

					venue.distance = distance;

					// venue.type = "type"; //item.getString("type");

					venueList.add(venue);
				}
			}

		} catch (Exception ex) {
			throw ex;
		}

		return venueList;
	}

	private String streamToString(InputStream is) throws IOException {
		String str = "";

		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
			} finally {
				is.close();
			}

			str = sb.toString();
		}

		return str;
	}

	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public void getLocation() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		provider = mlocManager.getBestProvider(criteria, true);

		for (int i = 0; i < 25; i++) {
			mlocManager.requestLocationUpdates(provider, 0, 0, this);
			Log.i("info", "requesting updates");
		}

		Location location = mlocManager.getLastKnownLocation(provider);

		if (location == null) {
			mProgress.dismiss();
			String alert = "Couldn't get location";
			Toast toast = Toast.makeText(LocationsActivity.this, alert,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
			mlocManager.requestLocationUpdates(provider, 0, 0, this);
			return;
		} else {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();

			try {
				mNearbyList = getNearby(latitude, longitude);
				mAdapter.setData(mNearbyList);
				mListView.setAdapter(mAdapter);
				mProgress.dismiss();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void onLocationChanged(Location location) {

	}

	public void onProviderDisabled(String provider) {

	}

	public void onProviderEnabled(String provider) {

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
