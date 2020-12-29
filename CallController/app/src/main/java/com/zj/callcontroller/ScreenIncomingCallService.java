package com.zj.callcontroller;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenIncomingCallService extends Service implements Runnable{

    public static final String EXTRA_PHONE_NUMBER = "extra_phone_number";
    public static final String EXTRA_SUBJECT = "extra_subject";
    public static final String EXTRA_BODY = "extra_body";

    private static String TAG = ScreenIncomingCallService.class.getName();
    private String phoneNumber;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null==intent){
            stopSelf(startId);
            return Service.START_NOT_STICKY;
        }
        phoneNumber = intent.getExtras().getString(EXTRA_PHONE_NUMBER);
        new Thread(this).start();
        return Service.START_STICKY;
    }

    private void rejectPhoneCall(){
        Log.i(TAG, "Rejecting phone call: "+phoneNumber);
        Intent intent = new Intent(this, CallControllService.class);
        intent.putExtra(CallControllService.EXTRA_ACTION, CallControllService.EXTRA_ACTION_REJECT);
        startService(intent);
    }

    private void answerPhoneCall(){
        Log.i(TAG, "Answering phone call: "+phoneNumber);
        Intent intent = new Intent(this, CallControllService.class);
        intent.putExtra(CallControllService.EXTRA_ACTION, CallControllService.EXTRA_ACTION_ANSWER);
        startService(intent);
    }

    static class PhoneNumberInfo {

        PhoneNumberInfo() {
            category = "unknown";
            from = "unknown";
            carrier = "unknown";
            spamValue = -1;
        }

        public String category;
        public String from;
        public String carrier;
        public int spamValue;
    }

   	/*
	 * return -1: exception happened 0: not a spam (unknown) 1: a spam from out
	 * of Shanghai 2: a spam from Shanghai
	 */

    @NonNull
    private PhoneNumberInfo getPhoneNumberInfo(String phoneNumber) {
        PhoneNumberInfo pni = new PhoneNumberInfo();
        try {
            pni = callPhoneNumberService(phoneNumber);
        } catch (IOException|JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, "Number type: " + pni.category + " from: " + pni.from + " spamValue: " + pni.spamValue + "operator: "+pni.carrier);
        return pni;
    }

    @NonNull
    private PhoneNumberInfo callPhoneNumberService(String phoneNumber) throws IOException, JSONException {
        HttpURLConnection huc = (HttpURLConnection) new URL(String.format(getString(R.string.phone_screen_rest_url)
                , phoneNumber)).openConnection();
        huc.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String l = br.readLine();
            if (null == l) {
                break;
            }
            sb.append(l);
        }
        JSONObject jsonObj =  new JSONObject(sb.toString());
        PhoneNumberInfo pni = new PhoneNumberInfo();
        pni.category = jsonObj.getString("category");
        pni.from = jsonObj.getString("from");
        pni.carrier = jsonObj.getString("carrier");
        pni.spamValue = jsonObj.getInt("spamValue");
        return pni;
    }

    @Override
    public void run() {
        String displayName = getContactName();
        StringBuilder sb = new StringBuilder("Call");
        sb.append(phoneNumber);

        if ("Unknown".equals(displayName)){
            PhoneNumberInfo pni = getPhoneNumberInfo(phoneNumber);
            sb.append(' ');
            sb.append(pni.from);
            sb.append(' ');
            sb.append(pni.carrier);
            sb.append(' ');
            sb.append(pni.category);
            if (2 == pni.spamValue) { // spam from SH, answer it
                if (android.os.Build.VERSION.SDK_INT > 23) {
                    sb.append(" auto answered");
                    answerPhoneCall();
                } else {
                    sb.append(" let it be");
                }
            } else if (1 == pni.spamValue) { // spam from out of SH, let it ring
                sb.append(" let it be");
            } else {
                sb.append(" forwarded");
                if (android.os.Build.VERSION.SDK_INT <= 23) {
                    rejectWithTelephony();
                } else {
                    rejectPhoneCall();
                }
            }
        }
        else{// known contact in address book
            sb.append(displayName);
            sb.append(" forwarded");
            if (android.os.Build.VERSION.SDK_INT <= 23) {
                rejectWithTelephony();
            } else {
                rejectPhoneCall();
            }
        }
        sendEmail(sb.toString(),(new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date()));
        stopSelf();
    }

    private void rejectWithTelephony() {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyInterface = m.invoke(tm);
            Class telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
            methodEndCall.invoke(telephonyInterface);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch(NoSuchMethodException  e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String getContactName() {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor.getCount() > 1) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else {
            return "Unknown";
        }
    }

    private void sendEmail(String subject, String body){
        Intent sendMailIntent = new Intent();
        sendMailIntent.setComponent(new ComponentName("com.zj.emailnotification", "com.zj.emailnotification.SendEmailService"));
        sendMailIntent.putExtra(EXTRA_SUBJECT, subject);
        sendMailIntent.putExtra(EXTRA_BODY,body);
        startService(sendMailIntent);
    }
}
