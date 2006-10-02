package org.seasar.cms.ymir.extension.creator;

public interface AnnotatedDesc {

    AnnotationDesc getAnnotationDesc(String name);

    void setAnnotationDesc(AnnotationDesc annotationDesc);

    AnnotationDesc[] getAnnotationDescs();
}
