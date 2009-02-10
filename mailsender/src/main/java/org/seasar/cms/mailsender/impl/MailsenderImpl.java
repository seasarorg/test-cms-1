package org.seasar.cms.mailsender.impl;

import javax.mail.internet.MimeMessage;

import org.seasar.cms.mailsender.Mailsender;
import org.seasar.cms.mailsender.MailsenderException;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;

import com.ozacc.mail.Mail;
import com.ozacc.mail.SendMail;

public class MailsenderImpl implements Mailsender {
    private SendMail sendMail;

    @Binding(bindingType = BindingType.MUST)
    public void setSendMail(SendMail sendMail) {
        this.sendMail = sendMail;
    }

    public void send(Mail... mails) throws MailsenderException {
        try {
            sendMail.send(mails);
        } catch (Throwable t) {
            throw new MailsenderException(t);
        }
    }

    public void send(MimeMessage... mimeMessages) throws MailsenderException {
        try {
            sendMail.send(mimeMessages);
        } catch (Throwable t) {
            throw new MailsenderException(t);
        }
    }
}
