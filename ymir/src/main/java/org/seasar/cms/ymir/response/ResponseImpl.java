package org.seasar.cms.ymir.response;

import java.io.InputStream;

public class ResponseImpl extends ResponseBase {

    private int type_ = TYPE_VOID;

    private String path_;

    private String contentType_;

    private InputStream inputStream_;

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
}
