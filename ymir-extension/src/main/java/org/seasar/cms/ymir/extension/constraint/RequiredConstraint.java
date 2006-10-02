package org.seasar.cms.ymir.extension.constraint;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.ConstraintViolationException;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.ConstraintViolationException.Message;

public class RequiredConstraint implements Constraint {

    private String[] names_;

    public RequiredConstraint(String[] names) {
        names_ = names;
    }

    public void confirm(Object component, Request request)
            throws ConstraintViolationException {
        List<Message> messageList = new ArrayList<Message>();
        for (int i = 0; i < names_.length; i++) {
            if (isEmpty(request, names_[i])) {
                messageList.add(new Message(PREFIX_MESSAGEKEY + "required",
                        new Object[] { names_[i] }));
            }
        }
        if (messageList.size() > 0) {
            throw new ConstraintViolationException().setMessages(messageList
                    .toArray(new Message[0]));
        }
    }

    boolean isEmpty(Request request, String name) {
        String value = request.getParameter(name);
        if (value != null && value.length() > 0) {
            return false;
        }
        FormFile file = request.getFileParameter(name);
        if (file != null && file.getSize() > 0) {
            return false;
        }
        return true;
    }
}
