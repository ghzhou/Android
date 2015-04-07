package com.zj.smsgateway;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SendMessageSocketService extends Service {

    SmsManager sms = SmsManager.getDefault();

    public SendMessageSocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new ServerThread()).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendSMS(String phoneNumber, String message)
    {
        try {
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        }catch(Exception e){
            Log.e("zhoujie",e.toString());
        }
    }

    class ServerThread implements Runnable {
        private ServerSocket serverSocket;
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(62000);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    Log.i("zhoujie","a client connected!");
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null==read){
                        Log.i("zhoujie","Got EOF, exit thread");
                        break;
                    }
                    Log.i("zhoujie", "Got request:" + read);
                    int i = read.indexOf(',');
                    if (i<5 || i+1 == read.length()){
                        Log.i("zhoujie","Invalid msg");
                        continue;
                    }
                    String to = read.substring(0,i);
                    String msg = read.substring(i+1);
                    sendSMS(to, msg);
                    //updateConversationHandler.post(new updateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
