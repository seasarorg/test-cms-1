package org.seasar.cms.framework.impl;

import java.io.InputStream;

import org.seasar.cms.framework.Response;

public class ResponseImpl implements Response {

    private static final String DEFAULT_CONTENTTYPE = "application/octet-stream";

    private int type_ = TYPE_VOID;

    private String path_;

    private String contentType_ = DEFAULT_CONTENTTYPE;

    private InputStream inputStream_;

    private int status_;

    public String getContentType() {

        return contentType_;
    }

    public void setContentType(String contentType) {

        contentType_ = contentType;
    }

    public InputStream getInputStream() {

        return inputStream_;
    }

    public void setInputStream(InputStream inputStream) {

        inputStream_ = inputStream;
    }

    public String getPath() {

        return path_;
    }

    public void setPath(String path) {

        path_ = path;
    }

    public int getType() {

        return type_;
    }

    public void setType(int type) {

        type_ = type;
    }

    public int getStatus() {

        return status_;
    }

    public void setStatus(int status) {

        status_ = status;
    }
}
