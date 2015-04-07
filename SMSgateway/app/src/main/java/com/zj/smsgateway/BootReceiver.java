package com.zj.smsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jack on 4/4/2015.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("zhoujie","boot event received!");
        Settings settings = Settings.getInstance(context.getSharedPreferences("MAIN",0));
        if(settings.isValid()){
            Mail.getInstance().setProperties(settings.getSmtpServer(), settings.getPortNumber(), settings.getEmailAddress(), settings.getPassword(), settings.getEmailRecipient());
        }
        Intent myIntent = new Intent(context, SendMessageSocketService.class);
        context.startService(myIntent);
    }
}
