package org.seasar.cms.beantable.impl;

import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

public class Hoge2 {

    private int id_;

    public int getId() {
        return id_;
    }

    @Id(value=IdType.SEQUENCE, sequenceName="hoge2_id")
    public void setId(int id) {
        id_ = id;
    }
}
