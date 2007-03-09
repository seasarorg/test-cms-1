package org.seasar.cms.ymir;

public class MessageResourcesNotFoundRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1741770699207141070L;

    private String messageResourcesName_;

    public MessageResourcesNotFoundRuntimeException() {
    }

    public MessageResourcesNotFoundRuntimeException(String message,
            Throwable cause) {

        super(message, cause);
    }

    public MessageResourcesNotFoundRuntimeException(String message) {

        super(message);
    }

    public MessageResourcesNotFoundRuntimeException(Throwable cause) {

        super(cause);
    }

    public String getMessageResourcesName() {

        return messageResourcesName_;
    }

    public MessageResourcesNotFoundRuntimeException setMessageResourcesName(
            String messageResourcesName) {

        messageResourcesName_ = messageResourcesName;
        return this;
    }
}
