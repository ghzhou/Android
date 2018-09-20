package com.zj.callcontroller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = PhoneStateReceiver.class.getName();
    private static String incomingNumber = null;
    private static boolean incomingFlag = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "phone state receiver invoked");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        switch (tm.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING:  // incoming call
                incomingFlag = true;
                incomingNumber = intent.getStringExtra("incoming_number");
                if (incomingNumber == null) {
                    break;
                }
                Log.d(TAG, "RINGING :" + incomingNumber);

                Intent screenIncomingCallIntent = new Intent(context, ScreenIncomingCallService.class);
                screenIncomingCallIntent.putExtra(ScreenIncomingCallService.EXTRA_PHONE_NUMBER,  incomingNumber);
                context.startService(screenIncomingCallIntent);
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
