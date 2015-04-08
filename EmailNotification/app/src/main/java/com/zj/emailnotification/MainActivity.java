package com.zj.emailnotification;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.jiezhou.smsgateway.R;


public class MainActivity extends ActionBarActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "App started!");
        settings = Settings.getInstance(getSharedPreferences("MAIN", 0));
        populateSettings();
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveSettings() && settings.isValid()){
                    Mail.getInstance().setProperties(settings.getSmtpServer(), settings.getPortNumber(), settings.getEmailAddress(), settings.getPassword(), settings.getEmailRecipient());
                }
            }
        });
    }

    private void populateSettings(){
        ((EditText) findViewById(R.id.email_address)).setText(settings.getEmailAddress());
        ((EditText) findViewById(R.id.smtp_server)).setText(settings.getSmtpServer());
        ((EditText) findViewById(R.id.port_number)).setText(settings.getPortNumber());
        ((EditText) findViewById(R.id.password)).setText(settings.getPassword());
        ((EditText) findViewById(R.id.email_recipient)).setText(settings.getEmailRecipient());
    }

    private boolean saveSettings(){
        String emailAddress = ((EditText) findViewById(R.id.email_address)).getText().toString().trim();
        String smtpServer= ((EditText) findViewById(R.id.smtp_server)).getText().toString().trim();
        String portNumber= ((EditText) findViewById(R.id.port_number)).getText().toString().trim();
        String password= ((EditText) findViewById(R.id.password)).getText().toString().trim();
        String emailRecipient = ((EditText) findViewById(R.id.email_recipient)).getText().toString().trim();

        settings.setEmailAddress(emailAddress);
        settings.setSmtpServer(smtpServer);
        settings.setPortNumber(portNumber);
        settings.setPassword(password);
        settings.setEmailRecipient(emailRecipient);
        return settings.saveSettings();
    }
}
