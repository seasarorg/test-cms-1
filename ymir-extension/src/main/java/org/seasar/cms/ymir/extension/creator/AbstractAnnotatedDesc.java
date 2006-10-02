package org.seasar.cms.ymir.extension.creator;

import java.util.Map;
import java.util.TreeMap;

public class AbstractAnnotatedDesc implements AnnotatedDesc {

    private Map<String, AnnotationDesc> annotationDescMap_ = new TreeMap<String, AnnotationDesc>();

    public AnnotationDesc getAnnotationDesc(String name) {
        return annotationDescMap_.get(name);
    }

    public AnnotationDesc[] getAnnotationDescs() {
        return annotationDescMap_.values().toArray(new AnnotationDesc[0]);
    }

    public void setAnnotationDesc(AnnotationDesc annotationDesc) {
        annotationDescMap_.put(annotationDesc.getName(), annotationDesc);
    }
}
