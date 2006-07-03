package org.seasar.cms.framework.creator.impl;

import org.seasar.cms.framework.creator.AbstractClassDesc;

public class SimpleClassDesc extends AbstractClassDesc {

    private String name_;

    public SimpleClassDesc(String name) {

        setName(name);
    }

    public Object clone() {

        return super.clone();
    }

    public String getName() {

        return name_;
    }

    public void setName(String name) {

        name_ = name;
    }
}
