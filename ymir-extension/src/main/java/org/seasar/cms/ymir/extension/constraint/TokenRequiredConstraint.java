package org.seasar.cms.ymir.extension.constraint;

import javax.servlet.http.HttpServletRequest;

import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.ConstraintViolatedException;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.ValidationFailedException;
import org.seasar.cms.ymir.YmirContext;
import org.seasar.cms.ymir.util.TokenUtils;

public class TokenRequiredConstraint implements Constraint {

    private String name_;

    public TokenRequiredConstraint(String name) {
        name_ = name;
    }

    public void confirm(Object component, Request request)
            throws ConstraintViolatedException {

        if (!TokenUtils.isTokenValid(getHttpServletRequest(), name_, true)) {
            throw new ValidationFailedException()
                    .addMessage(new ConstraintViolatedException.Message(
                            PREFIX_MESSAGEKEY + "tokenRequired", new Object[0]));
        }
    }

    HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) YmirContext.getYmir().getApplication()
                .getS2Container().getComponent(HttpServletRequest.class);
    }
}
