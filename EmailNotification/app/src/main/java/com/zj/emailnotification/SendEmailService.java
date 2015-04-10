package com.zj.emailnotification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SendEmailService extends Service {
    private static final String TAG = SendEmailService.class.getName();
    private Mail mail;
    private Settings settings;

    public SendEmailService() {
    }

    public boolean initializeMail(){
        settings.loadSettings();
        if (settings.isValid()){
            String smtpServer = settings.getSmtpServer();
            String portNumber = settings.getPortNumber();
            String emailAddress = settings.getEmailAddress();
            String password = settings.getPassword();
            String emailRecipient = settings.getEmailRecipient();
            mail.setProperties(smtpServer,portNumber,emailAddress,password,emailRecipient);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mail = Mail.getInstance();
        settings = Settings.getInstance(getContentResolver());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!initializeMail()) {
            Log.e(TAG,"Invalid email settings.");
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        String from = intent.getExtras().getString("from");
        String message = intent.getExtras().getString("message");
        if (from.length() == 0 && message.length() == 0) {
            Log.e(TAG,"Empty email message.");
            stopSelf(startId);
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
