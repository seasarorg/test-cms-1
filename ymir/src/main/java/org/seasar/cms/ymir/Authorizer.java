package org.seasar.cms.ymir;

public interface Authorizer {

    boolean authorize(Object component, Request request);
}
