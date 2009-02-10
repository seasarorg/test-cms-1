package org.seasar.cms.mailsender;

import javax.mail.internet.MimeMessage;

import com.ozacc.mail.Mail;

public interface Mailsender {
    void send(Mail... mails) throws MailsenderException;

    void send(MimeMessage... mimeMessages) throws MailsenderException;
}
