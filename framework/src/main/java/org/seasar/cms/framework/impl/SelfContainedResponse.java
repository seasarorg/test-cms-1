package org.seasar.cms.framework.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.seasar.cms.framework.Response;

public class SelfContainedResponse implements Response {

    private static final String DEFAULT_CONTENTTYPE = "application/octet-stream";

    private InputStream inputStream_;

    private String contentType_ = DEFAULT_CONTENTTYPE;

    public SelfContainedResponse() {
    }

    public SelfContainedResponse(InputStream inputStream) {

        setInputStream(inputStream);
    }

    public SelfContainedResponse(InputStream inputStream, String contentType) {

        setInputStream(inputStream);
        setContentType(contentType);
    }

    public SelfContainedResponse(String content) {

        if (content != null) {
            try {
                setInputStream(new ByteArrayInputStream(content
                    .getBytes("UTF-8")));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Can't happen!");
            }
            setContentType("text/html; charset=UTF-8");
        }
    }

    public int getType() {

        return TYPE_SELF_CONTAINED;
    }

    public void setType(int type) {

        throw new UnsupportedOperationException();
    }

    public String getPath() {

        return null;
    }

    public void setPath(String path) {

        throw new UnsupportedOperationException();
    }

    public InputStream getInputStream() {

        return inputStream_;
    }

    public void setInputStream(InputStream inputStream) {

        inputStream_ = inputStream;
    }

    public String getContentType() {

        return contentType_;
    }

    public void setContentType(String contentType) {

        contentType_ = contentType;
    }
}
