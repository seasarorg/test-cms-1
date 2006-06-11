package org.seasar.cms.framework.zpt;

public class DummyObject {

    private String className_;

    private boolean dto_;

    public DummyObject(String className, boolean dto) {

        className_ = className;
        dto_ = dto;
    }

    public String getClassName() {

        return className_;
    }

    public boolean isDto() {
        return dto_;
    }
}
