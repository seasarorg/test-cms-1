package org.seasar.cms.ymir.extension.creator.action.impl;


public class ClassDescDto {

    private boolean checked_;

    private String name_;

    public ClassDescDto(String name, boolean checked) {
        name_ = name;
        checked_ = checked;
    }

    public boolean isChecked() {
        return checked_;
    }

    public String getName() {
        return name_;
    }
}
