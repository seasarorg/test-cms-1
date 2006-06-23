package org.seasar.cms.framework.impl;

import java.io.InputStream;

import org.seasar.cms.framework.Response;

abstract public class ResponseBase implements Response {

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

        throw new UnsupportedOperationException();
    }

    public void setContentType(String contentType) {

        throw new UnsupportedOperationException();
    }

    public int getStatus() {

        throw new UnsupportedOperationException();
    }

    public void setStatus(int status) {

        throw new UnsupportedOperationException();
    }
}
