package org.seasar.cms.mailsender;

import java.util.List;

import org.seasar.cms.mailsender.annotation.BodyTemplate;

import com.ozacc.mail.Mail;

import freemarker.template.Configuration;

abstract public class HoeMailsender {
    abstract public void send(Mail mail) throws MailsenderException;

    abstract public void sendNoException(Mail mail);

    abstract public void send(Mail mail, Mail... mails)
            throws MailsenderException;

    public void send() {
    }

    @BodyTemplate("customer.ftl")
    abstract public void sendToCustomer(Mail mail, HoeDto dto);

    @BodyTemplate("customer.ftl")
    abstract public void sendToCustomer(Mail mail, HoeDto dto,
            Configuration configuration);

    @BodyTemplate("body.ftl")
    abstract public String evaluateBody(HoeDto dto);

    abstract public void send(Mail mail1, Mail[] mails, List<Mail> mailList);
}
