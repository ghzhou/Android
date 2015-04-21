package com.zj.callcontroller;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        public String tag;
        public String from;
        public String operator;
        public int spamValue;
    }

   	/*
	 * return -1: exception happened 0: not a spam (unknown) 1: a spam from out
	 * of Shanghai 2: a spam from Shanghai
	 */

    private PhoneNumberInfo  getPhoneNumberInfo(String phoneNumber){
			PhoneNumberInfo pni = new PhoneNumberInfo();
			pni.tag = "unknown";
			pni.from = "unknown";
            pni.operator = "unknown";
			pni.spamValue = -1;
			try {
				HttpURLConnection huc = (HttpURLConnection) new URL("http://www.sogou.com/web?query=" + phoneNumber).openConnection();
				HttpURLConnection.setFollowRedirects(true);
				huc.setConnectTimeout(5 * 1000);
				huc.setReadTimeout(1 * 1000);
				huc.setRequestMethod("GET");
				huc.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				huc.addRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
				huc.addRequestProperty("Cache-Control","no-cache");
				huc.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
				huc.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));

//                var queryphoneinfo = '号码通用户数据：骚扰电话：0'.replace(/：/g, ':');
//                var amount = '224';
//                var showmin = '5';
//                </script><script type="text/javascript" charset="utf-8" src="http://dl.web.sogoucdn.com/vr/js/491.min.91fe60ba.js"></script><script type="text/javascript">
//                define("", ["vr"], function(vr) {
//                    vr.add(491, "10001001", "", 0,"13250915116	浙江嘉兴 中国联通 ");
//                }

//                var queryphoneinfo = ''.replace(/：/g, ':');
//                var amount = '';
//                var showmin = '5';
//                </script><script type="text/javascript" charset="utf-8" src="http://dl.web.sogoucdn.com/vr/js/491.min.91fe60ba.js"></script><script type="text/javascript">
//                define("", ["vr"], function(vr) {
//                    vr.add(491, "20005701", "d07119069b7ed47119069b1d9761b0e91ddb1e6169e9119b", 0,"02150385222	上海");
//                }
//                );


                Pattern p1 = Pattern.compile("var queryphoneinfo = '号码通用户数据：(\\S+?)：");
                Pattern p2 = Pattern.compile("vr\\.add\\(\\d+?, \"\\d+?\", \"\\w*?\", \\d+?,\"\\d+?\\s+?(\\S+)(\\s(\\S+)\\s)?\"");

                while(true){
                    String l = br.readLine();
                    if (null == l ){
                        break;
                    }
                    Matcher m1 = p1.matcher(l);
                    if (m1.find()){
                        pni.tag = m1.group(1);
                    }
                    Matcher m2 = p2.matcher(l);
                    if (m2.find()){
                        pni.from = m2.group(1);
                        if (m2.group(2)!=null){
                            pni.operator = m2.group(2);
                        }
                        break;
                    }
                }

                pni.spamValue = 0;
                if (pni.tag.contains("中介") || pni.tag.contains("推销") || pni.tag.contains("骚扰") || pni.tag.contains("诈骗")) {
                    pni.spamValue++;
                    if (pni.from.contains("上海")) {
                        pni.spamValue++;
                    }
                }

			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
			Log.i(TAG, "Number type: " + pni.tag + " from: " + pni.from + " spamValue: " + pni.spamValue + "operator: "+pni.operator);
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
            sb.append(pni.operator);
            sb.append(' ');
            sb.append(pni.tag);
            if (2 == pni.spamValue) { // spam from SH, answer it
                sb.append(" auto answered");
                answerPhoneCall();
            } else if (1 == pni.spamValue) { // spam from out of SH, let it ring
                sb.append(" let it be");
            } else {
                sb.append(" forwarded");
                rejectPhoneCall();
            }
        }
        else{// known contact in address book
            sb.append(displayName);
            sb.append(" forwarded");
            rejectPhoneCall();// reject it immediately so that the call will be forwarded asap.
        }
        sendEmail(sb.toString(),(new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date()));
        stopSelf();
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
