package com.zj.callcontroller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = PhoneStateReceiver.class.getName();
    private static String incomingNumber = null;
    private static boolean incomingFlag = false;

    private String getContactNameByPhoneNumber(Context context, String phoneNumber){

        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor.getCount()>1) {
            cursor.moveToFirst();
            return cursor.getString(0);
        }
        else{
            return "Unknown";
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "phone state receiver invoked");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        switch (tm.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING:  // incoming call
                incomingFlag = true;
                incomingNumber = intent.getStringExtra("incoming_number");
                Log.d(TAG, "RINGING :" + incomingNumber);
                getContactNameByPhoneNumber(context,incomingNumber);

                Intent sendMailIntent = new Intent();
                sendMailIntent.setComponent(new ComponentName("com.zj.emailnotification", "com.zj.emailnotification.SendEmailService"));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                sendMailIntent.putExtra("from","Got incoming call from: "+incomingNumber + '('+getContactNameByPhoneNumber(context,incomingNumber)+") at " + currentDateandTime);
                sendMailIntent.putExtra("message","as title");
                context.startService(sendMailIntent);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (incomingFlag) {
                    incomingNumber = intent.getStringExtra("incoming_number");
                    Log.d(TAG, "incoming ACCEPT :" + incomingNumber);
                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                if (incomingFlag) {     // hang up
                    incomingNumber = intent.getStringExtra("incoming_number");
                    Log.d(TAG, "incoming IDLE, number:" + incomingNumber);
                }
                break;
        }
    }
}
