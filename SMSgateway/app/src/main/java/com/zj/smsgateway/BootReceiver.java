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
        Intent myIntent = new Intent(context, SendMessageSocketService.class);
        context.startService(myIntent);
    }
}
