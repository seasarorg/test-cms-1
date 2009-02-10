package org.seasar.cms.mailsender;

import javax.mail.internet.MimeMessage;

import com.ozacc.mail.Mail;
import com.ozacc.mail.MailException;
import com.ozacc.mail.SendMail;

public class HoeSendMail implements SendMail {
    private Mail[] mails;
    private MimeMessage[] messages;
    private boolean throwingException;

    public void send(Mail mail) throws MailException {
        if (throwingException) {
            throw new MailException("exception");
        }
        mails = new Mail[] { mail };
    }

    public void send(Mail[] mails) throws MailException {
        if (throwingException) {
            throw new MailException("exception");
        }
        this.mails = mails;
    }

    public void send(MimeMessage message) throws MailException {
        if (throwingException) {
            throw new MailException("exception");
        }
        messages = new MimeMessage[] { message };
    }

    public void send(MimeMessage[] messages) throws MailException {
        if (throwingException) {
            throw new MailException("exception");
        }
        this.messages = messages;
    }

    public Mail[] getMails() {
        return mails;
    }

    public MimeMessage[] getMimeMessages() {
        return messages;
    }

    public void setThrowingException(boolean throwingException) {
        this.throwingException = throwingException;
    }
}
