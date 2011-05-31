package org.seasar.cms.beantable.impl;

import org.seasar.cms.beantable.annotation.VersionNo;

public class Hoge3 {
    private int versionNo_;

    @VersionNo
    public int getVersionNo() {
        return versionNo_;
    }

    public void setVersionNo(int versionNo) {
        versionNo_ = versionNo;
    }
}
