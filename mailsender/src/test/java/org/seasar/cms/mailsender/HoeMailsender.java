package org.seasar.cms.mailsender;

import java.util.List;

import org.seasar.cms.mailsender.annotation.BodyTemplate;
import org.seasar.cms.mailsender.annotation.Subject;

import com.ozacc.mail.Mail;

import freemarker.template.Configuration;

abstract public class HoeMailsender {
    abstract public void send(Mail mail) throws MailsenderException;

    abstract public void sendNoException(Mail mail);

    abstract public void send(Mail mail, Mail... mails)
            throws MailsenderException;

    public void send() {
    }

    @Subject(template = "subject.ftl")
    @BodyTemplate("customer.ftl")
    abstract public void sendToCustomer(Mail mail, HoeDto dto);

    @Subject("件名")
    @BodyTemplate("customer.ftl")
    abstract public void sendToCustomer(Mail mail, HoeDto dto,
            Configuration configuration);

    @BodyTemplate("body.ftl")
    abstract public String evaluateBody(HoeDto dto);

    abstract public void send(Mail mail1, Mail[] mails, List<Mail> mailList);

    @Subject(template = "subject.ftl")
    abstract public String evaluateSubject(HoeDto dto);
}
