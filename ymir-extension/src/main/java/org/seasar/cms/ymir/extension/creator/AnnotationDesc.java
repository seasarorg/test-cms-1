package org.seasar.cms.ymir.extension.creator;

public interface AnnotationDesc extends Cloneable {

    Object clone();

    String getName();

    String getBody();

    void setBody(String body);
}
