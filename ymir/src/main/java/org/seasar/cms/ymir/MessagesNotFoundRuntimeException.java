package org.seasar.cms.ymir;

public class MessagesNotFoundRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1741770699207141070L;

    private String messagesName_;

    public MessagesNotFoundRuntimeException() {
    }

    public MessagesNotFoundRuntimeException(String message, Throwable cause) {

        super(message, cause);
    }

    public MessagesNotFoundRuntimeException(String message) {

        super(message);
    }

    public MessagesNotFoundRuntimeException(Throwable cause) {

        super(cause);
    }

    public String getMessagesName() {

        return messagesName_;
    }

    public MessagesNotFoundRuntimeException setMessagesName(String messagesName) {

        messagesName_ = messagesName;
        return this;
    }
}
