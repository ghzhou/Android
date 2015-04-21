package com.zj.smsgateway;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendMessageSocketService extends Service {

    private static String TAG = SendMessageSocketService.class.getName();
    private Method sendTextMessage = null;
    private Object smsManager=null;


    public SendMessageSocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // I am using a dual sim phone, I have to leverage reflection to get the non-public method in order to specify the SIM  to send a message.
        // After API level 18 (Android 4.3), there is a subscription manager which makes our life easier, with the new subscription manager, you can get the SmsManager
        // for a specified subscription
        // SmsManager sms = SmsManager.getDefault();
        // sms.sendTextMessage(phoneNumber, null, message, null, null);
        //
        try {
            Class<?> smsManagerClass = null;
            Class[] sendTextMessageParams = {String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, int.class};
            smsManagerClass = Class.forName("android.telephony.SmsManager");

//        Method[] m = smsManagerClass.getDeclaredMethods();
//        for (int i = 0; i < m.length; i++)
//            Log.d(TAG, m[i].toString());

            Method method = smsManagerClass.getMethod("getDefault", new Class[]{});
            smsManager = method.invoke(smsManagerClass, new Object[]{});
            sendTextMessage = smsManagerClass.getMethod("sendTextMessage", sendTextMessageParams);
        } catch (ClassNotFoundException|NoSuchMethodException|InvocationTargetException|IllegalAccessException e) {
            Log.e(TAG,e.getMessage());
        }

        new Thread(new ServerThread()).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendSMS(String phoneNumber, String message, int type)
    {
        try {
            sendTextMessage.invoke(smsManager, phoneNumber, null,message, null, null, type);
        }catch(Exception e){
            Log.e(TAG,e.toString());
        }
    }

    class ServerThread implements Runnable {
        private ServerSocket serverSocket;
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(62000);
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    Log.i(TAG,"a client connected!");
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
        }
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String line = input.readLine();
                    if (null==line){
                        Log.i(TAG,"Got EOF, exit thread");
                        break;
                    }
                    Log.i(TAG, "Got request:" + line);
                    Pattern p = Pattern.compile("^(\\d+?),(.*),(\\d)$");
                    Matcher m = p.matcher(line);
                    if (m.find()){
                        sendSMS(m.group(1), m.group(2), Integer.parseInt(m.group(3)));
                    }
                    else{
                        Log.i(TAG,"Invalid msg");
                    }
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        }
    }
}
