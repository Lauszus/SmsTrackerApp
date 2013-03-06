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
	private StringBuilder address = new StringBuilder();
	private StringBuilder body = new StringBuilder();

    @Override
    public void onReceive(Context context, Intent intent) {    	
    	if(intent.getAction().equals(SMS_ACTION)) {
    		Bundle bundle = intent.getExtras(); // Get the SMS message passed in
    		
    		if (bundle != null) {
    			Object[] smsExtra = (Object[]) bundle.get(SMS_EXTRA_NAME); // Retrieve the SMS message received
    			SmsMessage[] msgs = new SmsMessage[smsExtra.length];
	            for (int i=0; i<msgs.length; i++) {
	                msgs[i] = SmsMessage.createFromPdu((byte[])smsExtra[i]);
	                address.append(msgs[i].getOriginatingAddress());
	                body.append(msgs[i].getMessageBody().toString());
	            }
	            Log.i(TAG, "smsReceive: " + address + " : " + body);
	            if(address.equals(R.string.phoneNumber)) { // Compare to the phone number
	            	try {
		            	String[] latlngStr = body.toString().replaceAll("[^(0-9|,|.)]", "").split(",",2);
		            	if(latlngStr.length < 2) {
		            		Log.i(TAG,"Failed to split string");
		            		return;
		            	}
			            Log.i(TAG,"Coordinates: " + latlngStr[0] + "," + latlngStr[1]);
		            	WeatherBalloonActivity.newMapMarker(new LatLng(Double.parseDouble(latlngStr[0]),Double.parseDouble(latlngStr[1])), null);
		            } catch (NullPointerException e) {
		            	Log.i(TAG,"Body is empty");
		            } catch(NumberFormatException e) {
		            	Log.i(TAG,"Failed to parse double");
		            }
	            }
	        }
	    }
    }
}
