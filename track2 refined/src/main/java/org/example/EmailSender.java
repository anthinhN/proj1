package org.example;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

public class EmailSender {

    public static void sendEmail(List<String> to, String subject, String body) {
        // Email information
        String host = "smtp.gmail.com";
        final String user = ""; // change this email to the one you want
        final String password = ""; // get the key-ID/password in the corresponding platform you are using

        // set SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // connect with server
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // create message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            for (String recipient : to) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }

            message.setSubject(subject);
            message.setText(body);



            // sending email
            Transport.send(message);

            System.out.println("Email sent successfully");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<String> test = List.of("", "","");
        // Call it to test sendEmail
        sendEmail(test, "This is subject", "This is the body of the email");
    }
}

