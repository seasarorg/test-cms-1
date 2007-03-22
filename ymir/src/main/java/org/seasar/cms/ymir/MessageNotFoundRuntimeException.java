package org.seasar.cms.ymir;

import java.util.Locale;

public class MessageNotFoundRuntimeException extends RuntimeException {

    private String messagesName_ = Globals.NAME_MESSAGES;

    private String messageKey_;

    private Locale locale_;

    private static final long serialVersionUID = 4840369946638949359L;

    public MessageNotFoundRuntimeException() {
    }

    public MessageNotFoundRuntimeException(String message, Throwable cause) {

        super(message, cause);

    }

    public MessageNotFoundRuntimeException(String message) {

        super(message);
    }

    public MessageNotFoundRuntimeException(Throwable cause) {

        super(cause);
    }

    public Locale getLocale() {

        return locale_;
    }

    public MessageNotFoundRuntimeException setLocale(Locale locale) {

        locale_ = locale;
        return this;
    }

    public String getMessageKey() {

        return messageKey_;
    }

    public MessageNotFoundRuntimeException setMessageKey(String messageKey) {

        messageKey_ = messageKey;
        return this;
    }

    public String getMessagesName() {

        return messagesName_;
    }

    public MessageNotFoundRuntimeException setMessagesName(String messagesName) {

        messagesName_ = messagesName;
        return this;
    }
}
