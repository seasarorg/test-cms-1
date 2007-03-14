package org.seasar.cms.ymir.extension.constraint;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.ConstraintViolatedException;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.ConstraintViolatedException.Message;

public class NumericConstraint implements Constraint {

    private String[] names_;

    private boolean integer_;

    private Double greaterEdge_;

    private boolean greaterIncludeEqual_;

    private Double lessEdge_;

    private boolean lessIncludeEqual_;

    public NumericConstraint(String[] names, boolean integer,
            Double greaterEdge, boolean greaterIncludeEqual, Double lessEdge,
            boolean lessIncludeEqual) {
        names_ = names;
        integer_ = integer;
        greaterEdge_ = greaterEdge;
        greaterIncludeEqual_ = greaterIncludeEqual;
        lessEdge_ = lessEdge;
        lessIncludeEqual_ = lessIncludeEqual;
    }

    public void confirm(Object component, Request request)
            throws ConstraintViolatedException {
        List<Message> messageList = new ArrayList<Message>();
        for (int i = 0; i < names_.length; i++) {
            confirm(request, names_[i], messageList);
        }
        if (messageList.size() > 0) {
            throw new ConstraintViolatedException().setMessages(messageList
                    .toArray(new Message[0]));
        }
    }

    void confirm(Request request, String name, List<Message> messageList) {
        String key = PREFIX_MESSAGEKEY + "numeric";
        String[] values = request.getParameterValues(name);
        if (values == null) {
            return;
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i].length() == 0) {
                continue;
            }
            if (integer_ && values[i].indexOf('.') >= 0) {
                messageList.add(new Message(key, new Object[] { name }));
            }
            double value;
            try {
                value = Double.parseDouble(values[i]);
            } catch (NumberFormatException ex) {
                messageList.add(new Message(key, new Object[] { name }));
                continue;
            }
            if (greaterEdge_ != null) {
                if (greaterIncludeEqual_) {
                    if (value < greaterEdge_.doubleValue()) {
                        messageList.add(new Message(key + ".greaterEqual",
                                new Object[] { name, greaterEdge_ }));
                    }
                } else {
                    if (value <= greaterEdge_.doubleValue()) {
                        messageList.add(new Message(key + ".greaterThan",
                                new Object[] { name, greaterEdge_ }));
                    }
                }
            }
            if (lessEdge_ != null) {
                if (lessIncludeEqual_) {
                    if (value > lessEdge_.doubleValue()) {
                        messageList.add(new Message(key + ".lessEqual",
                                new Object[] { name, lessEdge_ }));
                    }
                } else {
                    if (value >= lessEdge_.doubleValue()) {
                        messageList.add(new Message(key + ".lessThan",
                                new Object[] { name, lessEdge_ }));
                    }
                }
            }
        }
    }
}
