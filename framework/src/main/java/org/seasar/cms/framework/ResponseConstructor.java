package org.seasar.cms.framework;

public interface ResponseConstructor {

    String getTargetClassName();

    Response constructResponse(Object value);
}
