package org.seasar.cms.ymir.impl;

abstract public class TransitionResponse extends ResponseBase {

    protected String path_;

    public TransitionResponse() {
    }

    public TransitionResponse(String path) {

        setPath(path);
    }

    public String getPath() {

        return path_;
    }

    public void setPath(String path) {

        path_ = path;
    }
}
