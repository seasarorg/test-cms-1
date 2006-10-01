package org.seasar.cms.ymir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstraintViolationException extends Exception {

    private static final long serialVersionUID = -7148377969598636958L;

    private String path_;

    private List messageList_ = new ArrayList();

    public ConstraintViolationException() {
    }

    public ConstraintViolationException(String message) {

        super(message);
    }

    public ConstraintViolationException(Throwable cause) {

        super(cause);

    }

    public ConstraintViolationException(String message, Throwable cause) {

        super(message, cause);

    }

    public String getPath() {

        return path_;
    }

    public ConstraintViolationException setPath(String path) {

        path_ = path;
        return this;
    }

    public boolean hasMessage() {

        return (messageList_.size() > 0);
    }

    public Message[] getMessages() {

        return (Message[]) messageList_.toArray(new Message[0]);
    }

    public ConstraintViolationException setMessages(Message[] messages) {

        messageList_.clear();
        messageList_.addAll(Arrays.asList(messages));
        return this;
    }

    public ConstraintViolationException addMessage(Message message) {

        messageList_.add(message);
        return this;
    }

    public ConstraintViolationException addMessage(String key,
            Object[] parameters) {

        return addMessage(new Message(key, parameters));
    }

    public static class Message {

        private String key_;

        private Object[] parameters_;

        public Message(String key, Object[] parameters) {
            key_ = key;
            parameters_ = parameters;
        }

        public String getKey() {
            return key_;
        }

        public Object[] getParameters() {
            return parameters_;
        }
    }
}
