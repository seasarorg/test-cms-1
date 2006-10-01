package org.seasar.cms.ymir.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class SelfContainedResponse extends ResponseBase {

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

    public String toString() {

        return "(self-contained)";
    }

    public int getType() {

        return TYPE_SELF_CONTAINED;
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
