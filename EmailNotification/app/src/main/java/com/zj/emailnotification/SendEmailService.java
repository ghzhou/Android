package com.zj.emailnotification;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
public class SendEmailService extends Service {

    private Looper looper;
    private EmailHandler emailHandler;
    private Mail mail;

    public SendEmailService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mail = Mail.getInstance();
        // Get the HandlerThread's Looper and use it for our Handler
        HandlerThread thread = new HandlerThread("EmailThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        looper = thread.getLooper();
        emailHandler = new EmailHandler(looper);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message msg = emailHandler.obtainMessage();
        msg.setData(intent.getBundleExtra("message"));
        msg.arg1=startId;
        emailHandler.sendMessage(msg);
        return START_STICKY;
    }

//    class SendEmailThread implements Runnable {
//
//        private String subject;
//        private String body;
//
//        public SendEmailThread(String subject, String body) {
//            this.subject = subject;
//            this.body = body;
//        }
//
//        @Override
//        public void run() {
//            mail.send(subject, body);
//        }
//    }
private final class EmailHandler extends Handler {

    public EmailHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        String subject = msg.getData().getString("subject");
        if (subject.length() == 0) {
            stopSelf(msg.arg1);
            return;
        }
        String body = msg.getData().getString("body");
        if (body.length() == 0) {
            stopSelf(msg.arg1);
            return;
        }
        mail.send(subject,body );
        stopSelf(msg.arg1);
    }
}

}
