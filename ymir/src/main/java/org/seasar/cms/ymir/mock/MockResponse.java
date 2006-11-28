package org.seasar.cms.ymir.mock;

import java.io.InputStream;

import org.seasar.cms.ymir.Response;

public class MockResponse implements Response {

    private String contentType_;

    private InputStream inputStream_;

    private String path_;

    private int status_;

    private int type_;

    private String characterEncoding_;

    public String getCharacterEncoding() {
        return characterEncoding_;
    }

    public String getContentType() {
        return contentType_;
    }

    public InputStream getInputStream() {
        return inputStream_;
    }

    public String getPath() {
        return path_;
    }

    public int getStatus() {
        return status_;
    }

    public int getType() {
        return type_;
    }

    public void setCharacterEncoding(String characterEncoding) {
        characterEncoding_ = characterEncoding;
    }

    public void setContentType(String contentType) {
        contentType_ = contentType;
    }

    public void setInputStream(InputStream inputStream) {
        inputStream_ = inputStream;
    }

    public void setPath(String path) {
        path_ = path;
    }

    public void setStatus(int status) {
        status_ = status;
    }

    public void setType(int type) {
        type_ = type;
    }
}
