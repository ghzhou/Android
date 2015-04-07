package com.zj.callcontroller;

import android.app.Service;
import android.content.BroadcastReceiver;
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
                if ("17717532975".equals(incomingNumber)){
                    disconnectCall();
                }
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

    public void disconnectCall() {
        try {
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            getService.setAccessible(true);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            serviceMethod.setAccessible(true);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.setAccessible(true);
            telephonyEndCall.invoke(telephonyObject);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,
                    "FATAL ERROR: could not connect to telephony subsystem");
            Log.e(TAG, "Exception object: " + e);
        }
    }

}
