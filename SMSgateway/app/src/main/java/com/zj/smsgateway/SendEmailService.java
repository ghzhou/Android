package com.zj.smsgateway;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SendEmailService extends Service {

    private Mail mail;

    public SendEmailService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mail = Mail.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String from = intent.getExtras().getString("from");
        if (null == from || from.length() == 0) {
            return START_NOT_STICKY;
        }
        String message = intent.getExtras().getString("message");
        if (null == message || message.length() == 0) {
            return START_NOT_STICKY;
        }
        new Thread(new SendEmailThread(from, message)).start();
        return START_NOT_STICKY;
    }

    class SendEmailThread implements Runnable {

        private String subject;
        private String body;

        public SendEmailThread(String subject, String body) {
            this.subject = subject;
            this.body = body;
        }

        @Override
        public void run() {
            mail.send(subject, body);
        }
    }
}
