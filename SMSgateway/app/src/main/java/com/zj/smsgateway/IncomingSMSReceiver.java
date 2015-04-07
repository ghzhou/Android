package com.zj.smsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class IncomingSMSReceiver extends BroadcastReceiver {

    private String TAG = IncomingSMSReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] msgs = new SmsMessage[pdus.length];
                    StringBuilder msgFrom = new StringBuilder();
                    StringBuilder msgBody = new StringBuilder();
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msgFrom.append(msgs[i].getOriginatingAddress());
                        msgBody.append(msgs[i].getMessageBody());
                    }
                    Log.i(TAG,msgFrom.toString());
                    Log.i(TAG,msgBody.toString());

                    Intent sendMailIntent = new Intent(context, SendEmailService.class);
                    sendMailIntent.putExtra("from",msgFrom.toString());
                    sendMailIntent.putExtra("message",msgBody.toString());
                    context.startService(sendMailIntent);
                }catch(Exception e){
                     Log.d(TAG, e.getMessage());
                }
            }
        }
    }
}