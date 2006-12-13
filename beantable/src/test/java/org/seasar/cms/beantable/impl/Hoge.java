package org.seasar.cms.beantable.impl;

import java.sql.Timestamp;

import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

public class Hoge {

    private Integer id_;

    private Timestamp postdate_;

    private String username_;

    private String comment_;

    public Hoge() {
    }

    public Hoge(Timestamp postdate, String username, String comment) {
        postdate_ = postdate;
        username_ = username;
        comment_ = comment;
    }

    public String getComment() {
        return comment_;
    }

    public void setComment(String comment) {
        comment_ = comment;
    }

    public Integer getId() {
        return id_;
    }

    @Id(IdType.IDENTITY)
    public void setId(Integer id) {
        id_ = id;
    }

    public Timestamp getPostdate() {
        return postdate_;
    }

    public void setPostdate(Timestamp postdate) {
        postdate_ = postdate;
    }

    public String getUsername() {
        return username_;
    }

    public void setUsername(String username) {
        username_ = username;
    }
}
