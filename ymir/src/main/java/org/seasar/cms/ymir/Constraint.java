package org.seasar.cms.ymir;

public interface Constraint {

    String PREFIX_MESSAGEKEY = "error.constraint.";

    void confirm(Object component, Request request)
            throws ConstraintViolationException;
}
