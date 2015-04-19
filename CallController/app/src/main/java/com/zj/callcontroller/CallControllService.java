package com.zj.callcontroller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class CallControllService extends Service implements Runnable{

    public static String EXTRA_ACTION = "action";
    public static String EXTRA_ACTION_REJECT="reject";
    public static String EXTRA_ACTION_ANSWER = "answer";
    public static String EXTRA_ACTION_TEST = "test";

    private static String TAG = CallControllService.class.getName();

    private String command = null;
    private AdbHost adbHost;

    @Override
    public void onCreate() {
        super.onCreate();
        adbHost = new AdbHost(CallControllService.this);
    }

    @Override
    public void onDestroy() {
        adbHost.close();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null==intent){
            return Service.START_STICKY;
        }

        String action=intent.getExtras().getString(EXTRA_ACTION);
        if (action.equals(EXTRA_ACTION_ANSWER)){
            command = "input keyevent KEYCODE_CALL\n";
        }
        else if(action.equals(EXTRA_ACTION_REJECT)){
            command = "input keyevent KEYCODE_ENDCALL\n";
        }
        else if(action.equals(EXTRA_ACTION_TEST)){
            command = "ls\n";
        }
        else{
            return Service.START_STICKY;
        }

        new Thread(this).start();

        return Service.START_STICKY;
    }

    @Override
    public void run() {
        try {
            adbHost.getAdbStream().write(command);
            while(true){
                byte[] bytes = adbHost.getAdbStream().read();
                if (bytes.length==0){
                    break;
                }
                Log.i(TAG,"Getting response from adb: "+new String(adbHost.getAdbStream().read()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to AdbHost(IOException):"+e.getMessage());
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to write to AdbHost(Interupt):" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to write to AdbHost(NosuchAlg):" + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
