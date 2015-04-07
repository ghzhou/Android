package com.zj.messenger;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SendMessageService extends IntentService {
    private static final String ACTION_SEND_MESSAGE = "com.zj.messenger.action.SEND_MESSAGE";

    private static final String IP = "com.zj.messenger.extra.IP";
    private static final String TO = "com.zj.messenger.extra.TO";
    private static final String MESSAGE = "com.zj.messenger.extra.MESSAGE";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSendMessage(Context context, String ip, String to, String message) {
        Intent intent = new Intent(context, SendMessageService.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra(IP, ip);
        intent.putExtra(TO, to);
        intent.putExtra(MESSAGE, message);
        context.startService(intent);
    }

    public SendMessageService() {
        super("SendMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_MESSAGE.equals(action)) {
                final String ip = intent.getStringExtra(IP);
                final String to = intent.getStringExtra(TO);
                final String message = intent.getStringExtra(MESSAGE);
                handleActionSendMessage(ip,to, message);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSendMessage(String ip, String to, String message) {
        try {
            Socket s=new Socket(ip,62000);
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            br.write(to+','+message);
            br.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
