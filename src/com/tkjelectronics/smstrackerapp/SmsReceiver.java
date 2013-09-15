package com.tkjelectronics.smstrackerapp;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

// Based on: http://colinyeoh.wordpress.com/2012/05/22/android-sms-receiver/

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SmsReceiver";
	public static final boolean D = SmsTrackerActivity.D;
	
	private static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private static final String SMS_EXTRA_NAME = "pdus";
	private SmsTrackerActivity mSmsTrackerActivity;

	public SmsReceiver(SmsTrackerActivity mSmsTrackerActivity) {
		this.mSmsTrackerActivity = mSmsTrackerActivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(SMS_ACTION) && getResultCode() == Activity.RESULT_OK) {
			Bundle bundle = intent.getExtras(); // Get the SMS message passed in

			if (bundle != null) {
				Object[] smsExtra = (Object[]) bundle.get(SMS_EXTRA_NAME); // Retrieve the SMS message received
				SmsMessage msgs = SmsMessage.createFromPdu((byte[]) smsExtra[smsExtra.length - 1]); // Get the newest message
				String address = msgs.getOriginatingAddress();
				String body = msgs.getMessageBody();
				if (D)
					Log.i(TAG, "Received sms from: " + address + "\nMessage: " + body);
				if (address.equals(context.getString(R.string.phoneNumber1)) || address.equals(context.getString(R.string.phoneNumber2))) { // Compare to the phone number
					try {
						String[] strings = body.replaceAll("[^(0-9|,|.|:)]", "").split(",", 3);
						if (strings.length != 3) {
							Log.i(TAG, "Failed to split string");
							return;
						}
						if (D)
							Log.i(TAG, "Time: " + strings[0] + " Coordinates: " + strings[1] + "," + strings[2]);
						mSmsTrackerActivity.newMapMarker(new LatLng(Double.parseDouble(strings[1]), Double.parseDouble(strings[2])), strings[0]);
						mSmsTrackerActivity.appendToLog(strings[0] + "," + strings[1] + "," + strings[2]);
						//mSmsTrackerActivity.sendSMS(address, "My coordinates are: " + mSmsTrackerActivity.lastCoordinates.toString()); // Send a SMS back as well
					} catch (NullPointerException e) {
						if (D)
							Log.i(TAG, "Body is empty");
					} catch (NumberFormatException e) {
						if (D)
							Log.i(TAG, "Failed to parse double");
					}
				}
			}
		}
	}
}
