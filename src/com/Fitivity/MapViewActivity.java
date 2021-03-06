package com.fitivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fitivity.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.parse.Parse;

public class MapViewActivity extends MapActivity implements LocationListener {

	private Location mostRecentLocation;
	private MapView map;
	private MyLocationOverlay me;
	private Button btnDone;
	String locationName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view);
		
		btnDone = (Button) findViewById(R.id.doneButton);
		
		//initialize the connection to parse
		Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");
		
		getLocation();
		map = (MapView)findViewById(R.id.map);

		if(mostRecentLocation != null)
			map.getController().setCenter(getPoint(mostRecentLocation.getLatitude(), mostRecentLocation.getLongitude()));
		else
			map.getController().setCenter(getPoint(42.2510, 71.8232));
		
		map.getController().setZoom(17);
		map.setBuiltInZoomControls(true);

		Drawable marker = getResources().getDrawable(R.drawable.marker);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

		map.getOverlays().add(new SitesOverlay(marker));

		me = new MyLocationOverlay(this, map);
		map.getOverlays().add(me);
		
		//when the done button is clicked on the map, ask the user what the name
		//of the new location is. Then store all the info and pass it back
		//to the LocationsActivity
		btnDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final EditText input = new EditText(MapViewActivity.this);
				  new AlertDialog.Builder(MapViewActivity.this)
				    .setTitle("Update Status")
				    .setMessage("What is the name of this location?")
				    .setView(input)
				    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            Editable value = input.getText();
				            locationName = value.toString();
				            Intent intent = new Intent();
				            intent.putExtra("locationName", locationName);
							intent.putExtra("latitude", mostRecentLocation.getLatitude());
							intent.putExtra("longitude", mostRecentLocation.getLongitude());
							setResult(123, intent);
							finish();
				        }
				    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            locationName = "Choose Again";
				        }
				    }).show();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		me.enableCompass();
	}

	@Override
	public void onPause() {
		super.onPause();

		me.disableCompass();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_S) {
			map.setSatellite(!map.isSatellite());
			return (true);
		} else if (keyCode == KeyEvent.KEYCODE_Z) {
			map.displayZoomControls(true);
			return (true);
		}

		return (super.onKeyDown(keyCode, event));
	}

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items = new ArrayList<OverlayItem>();
		private Drawable marker = null;
		private OverlayItem inDrag = null;
		private ImageView dragImage = null;
		private int xDragImageOffset = 0;
		private int yDragImageOffset = 0;
		private int xDragTouchOffset = 0;
		private int yDragTouchOffset = 0;

		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker = marker;
			dragImage = (ImageView) findViewById(R.id.drag);
			xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
			yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

			items.add(new OverlayItem(getPoint(mostRecentLocation.getLatitude(),mostRecentLocation.getLongitude()), "Drag Me", "Location"));

			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			boundCenterBottom(marker);
		}

		@Override
		public int size() {
			return (items.size());
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			boolean result = false;

			if (action == MotionEvent.ACTION_DOWN) {
				for (OverlayItem item : items) {
					Point p = new Point(0, 0);

					map.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x - p.x, y - p.y)) {
						result = true;
						inDrag = item;
						items.remove(inDrag);
						populate();

						xDragTouchOffset = 0;
						yDragTouchOffset = 0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset = x - p.x;
						yDragTouchOffset = y - p.y;

						break;
					}
				}
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				setDragImagePosition(x, y);
				result = true;
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);

				GeoPoint pt = map.getProjection().fromPixels(
						x - xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(),
						inDrag.getSnippet());

				items.add(toDrop);
				populate();

				inDrag = null;
				result = true;
			}

			return (result || super.onTouchEvent(event, mapView));
		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage.getLayoutParams();

			lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y - yDragImageOffset - yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}

	/**
	 * The Location Manager manages location providers. This code searches for
	 * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
	 * mechanism) and finds the last known location.
	 **/
	private void getLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = locationManager.getBestProvider(criteria, true);

		// In order to make sure the device is getting location, request
		// updates. locationManager.requestLocationUpdates(provider, 1, 0,
		// this);
		mostRecentLocation = locationManager.getLastKnownLocation(provider);
	}

	/** Sets the mostRecentLocation object to the current location of the device **/
	public void onLocationChanged(Location location) {
		mostRecentLocation = location;
	}

	/**
	 * The following methods are only necessary because WebMapActivity
	 * implements LocationListener
	 **/
	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}