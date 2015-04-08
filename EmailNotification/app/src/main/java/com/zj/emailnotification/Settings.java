package com.zj.emailnotification;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by jack on 4/6/2015.
 */
public class Settings {

    private String TAG = Settings.class.getName();
    private static Settings instance=null;

    public static String EMAIL_ADDRESS="email_address";
    public static String SMTP_SERVER="smtp_server";
    public static String PORT_NUMBER="port_number";
    public static String PASSWORD="password";
    public static String EMAIL_RECIPIENT = "email_recipient";

    private SharedPreferences settings;
    private String emailAddress;
    private String smtpServer;
    private String portNumber;
    private String password;
    private String emailRecipient;
    private boolean dirty;
    private SharedPreferences.Editor editor;

    private Settings(SharedPreferences settings){
        dirty=false;
        this.settings = settings;
        loadSettings();
    }

    public static Settings getInstance(SharedPreferences settings){
        if (null==instance){
            instance = new Settings(settings);
        }
        return instance;
    }

    private void loadSettings(){
        emailAddress = settings.getString(EMAIL_ADDRESS, "");
        smtpServer = settings.getString(SMTP_SERVER, "");
        portNumber = settings.getString(PORT_NUMBER, "");
        password = settings.getString(PASSWORD, "");
        emailRecipient = settings.getString(EMAIL_RECIPIENT,"");
    }

    public boolean isValid(){ // to be refactored , need to be more precise.
        return portNumber.length()>0 && password.length()>0 && smtpServer.length() >0 && emailAddress.length()>0 && emailRecipient.length()>0;
    }

    // return true means the settings has been changed
    public boolean saveSettings(){
        if(dirty){
            editor.apply();
            dirty=false;
            return true;
        };
        Log.d(TAG,"no change to settings");
        return false;
    }

    private void initializeEditor(){
        if(dirty) return;
        editor = settings.edit();
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
        initializeEditor();
        editor.putString(EMAIL_ADDRESS,emailAddress);
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        if(this.smtpServer.equals(smtpServer)){
            return;
        }
        this.smtpServer = smtpServer;
        initializeEditor();
        editor.putString(SMTP_SERVER,smtpServer);
    }

    public String getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(String portNumber) {
        if(this.portNumber.equals(portNumber)){
            return;
        }
        this.portNumber = portNumber;
        initializeEditor();
        editor.putString(PORT_NUMBER,portNumber);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if(this.password.equals(password)){
            return;
        }
        this.password = password;
        initializeEditor();
        editor.putString(PASSWORD,password);
    }

    public String getEmailRecipient() {
        return emailRecipient;
    }

    public void setEmailRecipient(String emailRecipient) {
        if(this.emailRecipient.equals(emailRecipient)){
            return;
        }
        this.emailRecipient = emailRecipient;
        initializeEditor();
        editor.putString(EMAIL_RECIPIENT,emailRecipient);
    }
}
