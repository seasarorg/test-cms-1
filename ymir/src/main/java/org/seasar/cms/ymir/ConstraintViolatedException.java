package org.seasar.cms.ymir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstraintViolatedException extends Exception {

    private static final long serialVersionUID = -7148377969598636958L;

    private String path_;

    private List messageList_ = new ArrayList();

    public ConstraintViolatedException() {
    }

    public ConstraintViolatedException(String message) {

        super(message);
    }

    public ConstraintViolatedException(Throwable cause) {

        super(cause);

    }

    public ConstraintViolatedException(String message, Throwable cause) {

        super(message, cause);

    }

    public String getPath() {

        return path_;
    }

    public ConstraintViolatedException setPath(String path) {

        path_ = path;
        return this;
    }

    public boolean hasMessage() {

        return (messageList_.size() > 0);
    }

    public Message[] getMessages() {

        return (Message[]) messageList_.toArray(new Message[0]);
    }

    public ConstraintViolatedException setMessages(Message[] messages) {

        messageList_.clear();
        messageList_.addAll(Arrays.asList(messages));
        return this;
    }

    public ConstraintViolatedException addMessage(Message message) {

        messageList_.add(message);
        return this;
    }

    public ConstraintViolatedException addMessage(String key,
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

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("key=").append(key_).append(", parameters={");
            for (int i = 0; i < parameters_.length; i++) {
                sb.append(parameters_[i]);
            }
            sb.append("}");
            return sb.toString();
        }

        public String getKey() {
            return key_;
        }

        public Object[] getParameters() {
            return parameters_;
        }
    }
}
