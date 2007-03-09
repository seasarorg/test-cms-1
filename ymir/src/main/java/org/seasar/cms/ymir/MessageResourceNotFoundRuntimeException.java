package org.seasar.cms.ymir;

import java.util.Locale;

public class MessageResourceNotFoundRuntimeException extends RuntimeException {

    private String messageResourcesName_;

    private String messageResourceKey_;

    private Locale locale_;

    private static final long serialVersionUID = 4840369946638949359L;

    public MessageResourceNotFoundRuntimeException() {
    }

    public MessageResourceNotFoundRuntimeException(String message,
            Throwable cause) {

        super(message, cause);

    }

    public MessageResourceNotFoundRuntimeException(String message) {

        super(message);
    }

    public MessageResourceNotFoundRuntimeException(Throwable cause) {

        super(cause);
    }

    public Locale getLocale() {

        return locale_;
    }

    public MessageResourceNotFoundRuntimeException setLocale(Locale locale) {

        locale_ = locale;
        return this;
    }

    public String getMessageResourceKey() {

        return messageResourceKey_;
    }

    public MessageResourceNotFoundRuntimeException setMessageResourceKey(
            String messageResourceKey) {

        messageResourceKey_ = messageResourceKey;
        return this;
    }

    public String getMessageResourcesName() {

        return messageResourcesName_;
    }

    public MessageResourceNotFoundRuntimeException setMessageResourcesName(
            String messageResourcesName) {

        messageResourcesName_ = messageResourcesName;
        return this;
    }
}
