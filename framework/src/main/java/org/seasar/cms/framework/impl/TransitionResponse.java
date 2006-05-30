package org.seasar.cms.framework.impl;

import java.io.InputStream;

import org.seasar.cms.framework.Response;

abstract public class TransitionResponse implements Response {

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

    public void setType(int type) {

        throw new UnsupportedOperationException();
    }

    public InputStream getInputStream() {

        throw new UnsupportedOperationException();
    }

    public void setInputStream(InputStream is) {

        throw new UnsupportedOperationException();
    }

    public String getContentType() {

        throw new UnsupportedOperationException();
    }

    public void setContentType(String contentType) {

        throw new UnsupportedOperationException();
    }
}
