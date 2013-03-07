package com.tkjelectronics.weatherballoonapp;

import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class WeatherBalloonActivity extends FragmentActivity implements LocationListener {
	private static final String TAG = "WeatherBalloonActivity";
	private LocationManager locationManager;
	
	private GoogleMap mMap;
	private Marker locationMarker;
	private boolean firstExactPosition;
	private SmsReceiver mSmsReceiver = new SmsReceiver(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		firstExactPosition = true;
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mSmsReceiver);
		locationManager.removeUpdates(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mSmsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			new AlertDialog.Builder(this)
					.setMessage("Your GPS seems to be disabled, do you want to enable it?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialog,final int id) {
									startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialog,final int id) {
									dialog.cancel();
									Toast.makeText(getApplicationContext(),"GPS must be on in order to use this application!", Toast.LENGTH_LONG).show();
									finish();									
								}
							})
					.create().show();
		} else {
			Toast.makeText(this,"Getting exact position...", Toast.LENGTH_SHORT).show();
			List<String> enabledProviders = locationManager.getProviders(true);
			for(String provider:enabledProviders) {
				Log.i(TAG, "Requesting location updates from: " + provider);
				this.locationManager.requestLocationUpdates(provider, 0, 0, this);
			}
			setUpMapIfNeeded();
		}
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				mMap.setMyLocationEnabled(true);
				mMap.getUiSettings().setAllGesturesEnabled(true);				
			} else {
				Toast.makeText(this,"Google Maps is not available!", Toast.LENGTH_LONG).show();
				finish();
			}
		}
		if(locationMarker == null) { // Set the marker to the last know position when the app is created
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(location != null) {
				LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 5));
				mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
				locationMarker = mMap.addMarker(new MarkerOptions().position(coordinates).title("Last known location"));
			}
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if(locationMarker != null)
			locationMarker.remove();
		LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
		Log.i(TAG,"onLocationChanged " + coordinates.toString() + " via " + location.getProvider() + " Accuracy: " + location.getAccuracy());
		//locationMarker = mMap.addMarker(new MarkerOptions().position(coordinates).title("Your exact location"));
		if(firstExactPosition) {
			firstExactPosition = false;
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
		}
	}
	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, "Provider " + provider + " is now disabled.");
	}
	@Override
	public void onProviderEnabled(String provider) {
		Log.i(TAG, "Provider " + provider + " is now enabled.");
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(TAG, "Provider " + provider + " has changed status to " + status);
	}
	
	public void newMapMarker(LatLng coordinates, String title) {
		if (mMap != null && coordinates != null) {
			firstExactPosition = false; // We don't care about the new position if there is a new position available
			Log.i(TAG,"newMapMarker " + coordinates.toString());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 5));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
			mMap.addMarker(new MarkerOptions().position(coordinates).title(title));
		}
	}
}
