package com.tkjelectronics.weatherballoonapp;

import com.google.android.gms.maps.model.LatLng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

// Based on: http://colinyeoh.wordpress.com/2012/05/22/android-sms-receiver/

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SmsReceiver";
	private static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private static final String SMS_EXTRA_NAME = "pdus";
	private String address = "";
	private String body = "";
	private WeatherBalloonActivity mWeatherBalloonActivity;

	public SmsReceiver(WeatherBalloonActivity weatherBalloonActivity) {
		mWeatherBalloonActivity = weatherBalloonActivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(SMS_ACTION)) {
			Bundle bundle = intent.getExtras(); // Get the SMS message passed in

			if (bundle != null) {
				Object[] smsExtra = (Object[]) bundle.get(SMS_EXTRA_NAME); // Retrieve the SMS message received
				SmsMessage msgs = SmsMessage.createFromPdu((byte[]) smsExtra[smsExtra.length - 1]); // Get the newest message
				address = msgs.getOriginatingAddress();
				body = msgs.getMessageBody();
				Log.i(TAG, "Received sms from: " + address + "\nMessage: " + body);
				if (address.equals(context.getString(R.string.phoneNumber1)) || address.equals(context.getString(R.string.phoneNumber2))) { // Compare to the phone number
					try {
						String[] latLngStr = body.replaceAll("[^(0-9|,|.)]", "").split(",", 2);
						if (latLngStr.length < 2) {
							Log.i(TAG, "Failed to split string");
							return;
						}
						Log.i(TAG, "Coordinates: " + latLngStr[0] + "," + latLngStr[1]);
						mWeatherBalloonActivity.newMapMarker(new LatLng(Double.parseDouble(latLngStr[0]),Double.parseDouble(latLngStr[1])),null);
					} catch (NullPointerException e) {
						Log.i(TAG, "Body is empty");
					} catch (NumberFormatException e) {
						Log.i(TAG, "Failed to parse double");
					}
				}
			}
		}
	}
}
