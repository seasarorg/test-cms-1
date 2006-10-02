package org.seasar.cms.ymir.extension.freemarker;

import org.seasar.cms.ymir.extension.annotation.SuppressConstraints;
import org.seasar.cms.ymir.extension.constraint.Required;

@Required("test")
public class HoePageBase {

    @Required("get")
    @SuppressConstraints
    public String getHoe() {
        return null;
    }

    @Required("set")
    public void setHoe(String hoe) {
    }

    @Required("method")
    public void _get() {
    }
}
