package org.seasar.cms.ymir.extension.constraint;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.ConstraintViolationException;
import org.seasar.cms.ymir.Request;

public class RequiredConstraint implements Constraint {

    private String[] names_;

    public RequiredConstraint(String[] names) {
        names_ = names;
    }

    public void confirm(Object component, Request request)
            throws ConstraintViolationException {
        List<Object> emptyNameList = new ArrayList<Object>();
        for (int i = 0; i < names_.length; i++) {
            if (isEmpty(request, names_[i])) {
                emptyNameList.add(names_[i]);
            }
        }
        if (emptyNameList.size() > 0) {
            throw new ConstraintViolationException().addMessage(
                    PREFIX_MESSAGEKEY + "required", emptyNameList.toArray());
        }
    }

    boolean isEmpty(Request request, String name) {
        if (request.getParameter(name) != null) {
            return true;
        } else if (request.getFileParameter(name) != null) {
            return true;
        } else {
            return false;
        }
    }
}
