package org.seasar.cms.mailsender;

public class MailsenderException extends Exception {
    private static final long serialVersionUID = 1L;

    public MailsenderException() {
    }

    public MailsenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailsenderException(String message) {
        super(message);
    }

    public MailsenderException(Throwable cause) {
        super(cause);
    }
}
