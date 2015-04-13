package com.zj.callcontroller;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScreenIncomingCallService extends Service implements Runnable{

    public static String EXTRA_PHONE_NUMBER = "extra_phone_number";
    private static String TAG = ScreenIncomingCallService.class.getName();
    private String phoneNumber;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        phoneNumber = intent.getExtras().getString(EXTRA_PHONE_NUMBER);
        new Thread(this).start();
        return Service.START_STICKY;
    }

    private OutputStream connectToAdbHost() throws IOException {
        Socket socket = new Socket("192.168.1.6",62001);
        return socket.getOutputStream();
    }

    private void rejectPhoneCall(){
        Log.i(TAG, "reject phone call from: "+phoneNumber);
        try {
            connectToAdbHost().write('R');
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to AdbHost");
        }
    }

    private void answerPhoneCall(){
        Log.i(TAG, "answer phone call from: "+phoneNumber);
        try {
            connectToAdbHost().write('A');
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to AdbHost");
        }
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

    private PhoneNumberInfo  getPhoneNumberInfo(String incomingCall){
			PhoneNumberInfo pni = new PhoneNumberInfo();
			pni.tag = "unknown";
			pni.from = "unknown";
            pni.operator = "unknown";
			pni.spamValue = -1;
			try {
				//HttpURLConnection huc = (HttpURLConnection) new URL("http://wap.sogou.com/web/searchList.jsp?keyword=" + incomingCall).openConnection();
				HttpURLConnection huc = (HttpURLConnection) new URL("http://www.sogou.com/web?query=" + incomingCall).openConnection();
				HttpURLConnection.setFollowRedirects(true);
				huc.setConnectTimeout(5 * 1000);
				huc.setReadTimeout(1 * 1000);
				huc.setRequestMethod("GET");
				huc.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				huc.addRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
				//huc.addRequestProperty("Accept-Encoding","gzip,deflate");
				huc.addRequestProperty("Cache-Control","no-cache");
				huc.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
				huc.connect();
				int length = huc.getContentLength();
				if (length==-1) {
					length = 1000000;
				}
				byte[] buffer = new byte[length+1];
				InputStream input = huc.getInputStream();
				int bytesRead = 0;
				while (true){
					if (bytesRead==length){
						break;
					}
					System.out.println("bytesRead:"+bytesRead+"Left:"+(length-bytesRead));
					int ret = input.read(buffer,bytesRead,length-bytesRead);
					if(ret==-1){
						break;
					}
					bytesRead+=ret;
				}
				buffer[bytesRead] = '\0';
				String s = new String(buffer, "UTF-8");
				Pattern p = Pattern.compile("号码通用户数据:(\\S+)'");

				Matcher m = p.matcher(s);
				if (m.find()) {
					pni.tag = m.group(1);
                    p = Pattern.compile("id=\"phonenumberdetail\">(\\S+)\\s(\\S+)");
                    m = p.matcher(s);
                    if(m.find()){
                        pni.from = m.group(1);
                        pni.operator = m.group(2);
                    }
					pni.spamValue = 0;
					if (pni.tag.contains("中介") || pni.tag.contains("推销") || pni.tag.contains("骚扰") || pni.tag.contains("诈骗")) {
						pni.spamValue++;
						if (pni.from.contains("上海")) {
							pni.spamValue++;
						}
					}
				} else {
					pni.spamValue = 0;
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
			Log.i(TAG, "Number type: " + pni.tag + " from: " + pni.from + " spamValue: " + pni.spamValue);
			return pni;
    }


    @Override
    public void run() {
        String displayName = getContactName();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateTime = sdf.format(new Date());
        StringBuilder sb = new StringBuilder(currentDateTime);
        sb.append(" incoming call: ");
        sb.append(phoneNumber);

        if ("Unknown".equals(displayName)){
            PhoneNumberInfo pni = getPhoneNumberInfo(phoneNumber);
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
        sendEmail(sb.toString(),"");
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
//        sendMailIntent.putExtra("from","Got incoming call from: "+phoneNumber + '('+getContactName()+") at " + currentDateandTime);
        sendMailIntent.putExtra("from",subject);
        sendMailIntent.putExtra("message",body);
        startService(sendMailIntent);
    }


}
