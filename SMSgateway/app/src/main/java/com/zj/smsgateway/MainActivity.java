package com.zj.smsgateway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.jiezhou.smsgateway.R;


public class MainActivity extends Activity {

    private String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent myIntent = new Intent(this, SendMessageSocketService.class);
        this.startService(myIntent);
    }

}
