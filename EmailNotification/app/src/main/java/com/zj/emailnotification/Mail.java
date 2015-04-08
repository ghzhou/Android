package com.zj.emailnotification;

import android.util.Log;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mail extends javax.mail.Authenticator {

    private static Mail instance;
    private Properties props;
    private String emailAddress;
    private String password;
    private String emailRecepient;
    private String TAG = Mail.class.getSimpleName();

    private Mail() {

        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public static Mail getInstance() {
        if (null == instance) {
            instance = new Mail();
        }
        return instance;
    }

    public void send(String subject, String body) {
        try {
            Session session = Session.getInstance(props, this);
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(emailAddress));
            msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(emailRecepient));
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // setup message body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            MimeMultipart _multipart = new MimeMultipart();
            _multipart.addBodyPart(messageBodyPart);

            // Put parts in message
            msg.setContent(_multipart);

            // send email
            Transport.send(msg);
            Log.i(TAG,"Email sent!");
        } catch (Exception e) {
            Log.e(TAG,"Exception when sending email:"+e.getMessage());
        }
    }

//    public void addAttachment(String filename) throws Exception {
//        BodyPart messageBodyPart = new MimeBodyPart();
//        DataSource source = new FileDataSource(filename);
//        messageBodyPart.setDataHandler(new DataHandler(source));
//        messageBodyPart.setFileName(filename);
//
//        _multipart.addBodyPart(messageBodyPart);
//    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(emailAddress, password);
    }

    public void setProperties(String smtpServer, String portNumber, String emailAddress, String password, String emailRecepient) {
        Log.i(TAG, "stmpServer=" + smtpServer + ";portNumber=" + portNumber + ";emailAddress=" + emailAddress);
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", portNumber);
        this.emailAddress = emailAddress;
        this.password = password;
        this.emailRecepient = emailRecepient;
    }
}