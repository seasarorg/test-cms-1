package org.seasar.cms.ymir.impl;

import java.io.InputStream;

import org.seasar.cms.ymir.Response;

abstract public class ResponseBase implements Response {

    private int status_ = STATUS_UNDEFINED;

    public void setType(int type) {

        throw new UnsupportedOperationException();
    }

    public String getPath() {

        throw new UnsupportedOperationException();
    }

    public void setPath(String path) {

        throw new UnsupportedOperationException();
    }

    public InputStream getInputStream() {

        throw new UnsupportedOperationException();
    }

    public void setInputStream(InputStream is) {

        throw new UnsupportedOperationException();
    }

    public String getContentType() {

        return null;
    }

    public void setContentType(String contentType) {

        throw new UnsupportedOperationException();
    }

    public int getStatus() {

        return status_;
    }

    public void setStatus(int status) {

        status_ = status;
    }
}
