package org.seasar.cms.mailsender.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ozacc.mail.Mail;
import com.ozacc.mail.MailException;
import com.ozacc.mail.SendMail;

public class MockSendMail implements SendMail {
    private static final Log log = LogFactory.getLog(MockSendMail.class);

    private static final String LS = System.getProperty("line.separator");

    public void send(Mail mail) throws MailException {
        log.info(LS + "********* Start sending mail ********");
        log.info(LS + render(mail));
        log.info("********* End sending mail ********" + LS);
    }

    public void send(Mail[] mails) throws MailException {
        for (Mail mail : mails) {
            send(mail);
        }
    }

    public void send(MimeMessage mimeMessage) throws MailException {
        log.info(LS + "********* Start sending mail (MimeMessage) ********");
        log.info(LS + render(mimeMessage));
        log.info("********* End sending mail (MimeMessage) ********" + LS);
    }

    public void send(MimeMessage[] mimeMessages) throws MailException {
        for (MimeMessage mimeMessage : mimeMessages) {
            send(mimeMessage);
        }
    }

    protected String render(Mail mail) {
        if (mail == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        InternetAddress from = mail.getFrom();
        if (from != null) {
            sb.append("From: ").append(from).append(LS).append(LS);
        }

        InternetAddress[] to = mail.getTo();
        if (to != null && to.length > 0) {
            sb.append("To: ");
            String delim = "";
            for (InternetAddress address : to) {
                sb.append(delim).append(address);
                delim = ", ";
            }
            sb.append(LS);
        }

        InternetAddress[] cc = mail.getCc();
        if (cc != null && cc.length > 0) {
            sb.append("Cc: ");
            String delim = "";
            for (InternetAddress address : cc) {
                sb.append(delim).append(address);
                delim = ", ";
            }
            sb.append(LS);
        }

        InternetAddress[] bcc = mail.getBcc();
        if (bcc != null && bcc.length > 0) {
            sb.append("Bcc: ");
            String delim = "";
            for (InternetAddress address : bcc) {
                sb.append(delim).append(address);
                delim = ", ";
            }
            sb.append(LS);
        }

        InternetAddress returnPath = mail.getReturnPath();
        if (returnPath != null) {
            sb.append("Return-Path: ").append(returnPath).append(LS);
        }

        String subject = mail.getSubject();
        if (subject != null) {
            sb.append("Subject: ").append(subject).append(LS);
        }

        sb.append(LS);

        sb.append(mail.getText());
        sb.append(LS);

        return sb.toString();
    }

    protected String render(MimeMessage mimeMessasge) {
        if (mimeMessasge == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        Address[] from;
        try {
            from = mimeMessasge.getFrom();
        } catch (MessagingException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (from != null && from.length > 0) {
            sb.append("From: ");
            String delim = "";
            for (Address address : from) {
                sb.append(delim).append(address);
                delim = ", ";
            }
            sb.append(LS);
        }

        List<Address> to = new ArrayList<Address>();
        List<Address> cc = new ArrayList<Address>();
        List<Address> bcc = new ArrayList<Address>();
        Address[] allRecipients;
        try {
            allRecipients = mimeMessasge.getAllRecipients();
        } catch (MessagingException ex) {
            throw new IllegalArgumentException(ex);
        }
        for (Address recipient : allRecipients) {
            String type = recipient.getType();
            if (RecipientType.TO.equals(type)) {
                to.add(recipient);
            } else if (RecipientType.CC.equals(type)) {
                cc.add(recipient);
            } else if (RecipientType.BCC.equals(type)) {
                bcc.add(recipient);
            } else {
                log.warn("Unknown recipient type found: " + type);
            }
        }

        if (to.size() > 0) {
            sb.append("To: ");
            String delim = "";
            for (Address address : to) {
                sb.append(delim).append(address);
                delim = ", ";
            }
            sb.append(LS);
        }

        if (cc.size() > 0) {
            sb.append("Cc: ");
            String delim = "";
            for (Address address : cc) {
                sb.append(delim).append(address);
                delim = ", ";
            }
            sb.append(LS);
        }

        if (bcc.size() > 0) {
            sb.append("Bcc: ");
            String delim = "";
            for (Address address : bcc) {
                sb.append(delim).append(address);
                delim = ", ";
            }
            sb.append(LS);
        }

        String subject;
        try {
            subject = mimeMessasge.getSubject();
        } catch (MessagingException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (subject != null) {
            sb.append("Subject: ").append(subject).append(LS);
        }

        sb.append(LS);

        try {
            sb.append(mimeMessasge.getContent()).append(LS);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        } catch (MessagingException ex) {
            throw new IllegalArgumentException(ex);
        }

        return sb.toString();
    }
}
