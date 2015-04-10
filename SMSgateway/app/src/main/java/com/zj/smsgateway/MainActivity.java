package com.zj.smsgateway;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.jiezhou.smsgateway.R;


public class MainActivity extends ActionBarActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private boolean serverStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
