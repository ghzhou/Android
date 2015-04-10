package com.zj.emailnotification;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Created by jack on 4/6/2015.
 */
public class Settings {

    private final String TAG = Settings.class.getName();
    private final Uri uri = Uri.parse("content://com.zj.emailnotification.provider/email_notification_setting/1");
    private static Settings instance=null;

    public static String EMAIL_ADDRESS="email_address";
    public static String SMTP_SERVER="smtp_server";
    public static String PORT_NUMBER="port_number";
    public static String PASSWORD="password";
    public static String EMAIL_RECIPIENT = "email_recipient";

    private ContentResolver contentResolver;
    private String emailAddress;
    private String smtpServer;
    private String portNumber;
    private String password;
    private String emailRecipient;
    private ContentValues newSettings;

    private boolean dirty;

    private Settings(ContentResolver contentResolver){
        dirty=false;
        this.contentResolver = contentResolver;
        loadSettings();
    }

    public static Settings getInstance(ContentResolver cr){
        if (null==instance){
            instance = new Settings(cr);
        }
        return instance;
    }

    public void loadSettings(){
        Cursor cur = contentResolver.query(uri, null, null, null, null);
        cur.moveToNext();
        smtpServer = cur.getString(1);
        portNumber = cur.getString(2);
        emailAddress = cur.getString(3);
        password = cur.getString(4);
        emailRecipient = cur.getString(5);
        cur.close();
    }

    public boolean isValid(){ // to be refactored , need to be more precise.
        return portNumber.length()>0 && password.length()>0 && smtpServer.length() >0 && emailAddress.length()>0 && emailRecipient.length()>0;
    }

    // return true means the settings has been changed
    public boolean saveSettings(){
        if(dirty){
            contentResolver.update(uri,newSettings,null,null);
            dirty=false;
            return true;
        };
        Log.d(TAG,"no change to settings");
        return false;
    }

    private void initializeNewSetting(){
        if(dirty) return;
        newSettings = new ContentValues();
        dirty=true;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        if(this.emailAddress.equals(emailAddress)){
            return;
        }
        this.emailAddress = emailAddress;
        initializeNewSetting();
        newSettings.put(EMAIL_ADDRESS, emailAddress);
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        if(this.smtpServer.equals(smtpServer)){
            return;
        }
        this.smtpServer = smtpServer;
        initializeNewSetting();
        newSettings.put(SMTP_SERVER, smtpServer);
    }

    public String getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(String portNumber) {
        if(this.portNumber.equals(portNumber)){
            return;
        }
        this.portNumber = portNumber;
        initializeNewSetting();
        newSettings.put(PORT_NUMBER, portNumber);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if(this.password.equals(password)){
            return;
        }
        this.password = password;
        initializeNewSetting();
        newSettings.put(PASSWORD, password);
    }

    public String getEmailRecipient() {
        return emailRecipient;
    }

    public void setEmailRecipient(String emailRecipient) {
        if(this.emailRecipient.equals(emailRecipient)){
            return;
        }
        this.emailRecipient = emailRecipient;
        initializeNewSetting();
        newSettings.put(EMAIL_RECIPIENT, emailRecipient);
    }
}
