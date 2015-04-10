package com.zj.callcontroller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "zhoujie";
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
                Log.d(TAG, "RINGING :" + incomingNumber);

                Intent sendMailIntent = new Intent();
                sendMailIntent.setComponent(new ComponentName("com.zj.emailnotification", "com.zj.emailnotification.SendEmailService"));
                sendMailIntent.putExtra("from","Got incoming call from: "+incomingNumber);
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
