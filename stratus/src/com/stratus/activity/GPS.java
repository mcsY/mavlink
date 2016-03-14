package com.stratus.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class GPS extends Service implements LocationListener
{
	private LocationManager locationManager;
	private float latitude;
	private float longitude;
	private float accuracy = 1000;
	private boolean firstFix = false;
	
	public GPS(Context context)
	{
		locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
	}
	
	@Override
	public void onLocationChanged(Location location)
	{
		if (location == null) return;
		firstFix = true;
		latitude = (float)location.getLatitude();
		longitude = (float)location.getLongitude();
		accuracy = (float)location.getAccuracy();
	}
	
	public void stopGPS()
	{
		locationManager.removeUpdates(this);
	}
	
	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public float getAccuracy() {
		return accuracy;
	}
	
	public String getGPSStatus()
	{
		if(!firstFix)
		{
			return "No Fix";
		}
		else if(accuracy > 20)
		{
			return "Poor";
		}
		else if(accuracy > 10)
		{
			return "Fair";
		}
		return "Good";
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}