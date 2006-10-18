package org.seasar.cms.ymir.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class SelfContainedResponse extends ResponseBase {

    public static final String DEFAULT_BINARY_CONTENTTYPE = "application/octet-stream";

    public static final String DEFAULT_ASCII_CONTENTTYPE = "text/html; charset=UTF-8";

    private InputStream inputStream_;

    private String string_;

    private String contentType_;

    public SelfContainedResponse() {
    }

    public SelfContainedResponse(InputStream inputStream) {

        this(inputStream, DEFAULT_BINARY_CONTENTTYPE);
    }

    public SelfContainedResponse(InputStream inputStream, String contentType) {

        setInputStream(inputStream);
        setContentType(contentType);
    }

    public SelfContainedResponse(String string) {

        this(string, DEFAULT_ASCII_CONTENTTYPE);
    }

    public SelfContainedResponse(String string, String contentType) {

        setString(string);
        setContentType(contentType);
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("Content-Type: ").append(contentType_).append(", Content: ");
        if (string_ != null) {
            sb.append(string_);
        } else {
            sb.append("(inputStream)");
        }
        return sb.toString();
    }

    public int getType() {

        return TYPE_SELF_CONTAINED;
    }

    public InputStream getInputStream() {

        InputStream inputStream;
        if (inputStream_ != null) {
            inputStream = inputStream_;
            inputStream_ = null;
        } else if (string_ != null) {
            String charset = getCharacterEncoding();
            if (charset == null) {
                charset = "ISO-8859-1";
            }
            try {
                inputStream = new ByteArrayInputStream(string_
                        .getBytes(charset));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Unknown charset: " + charset, ex);
            }
        } else {
            inputStream = null;
        }
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {

        setInputStream0(inputStream);
        setString0(null);
    }

    void setInputStream0(InputStream inputStream) {

        if (inputStream_ != null) {
            try {
                inputStream_.close();
            } catch (IOException ignore) {
            }
        }
        inputStream_ = inputStream;
    }

    public String getString() {

        return string_;
    }

    public void setString(String string) {

        setInputStream0(null);
        setString0(string);
    }

    void setString0(String string) {

        string_ = string;
    }

    public String getContentType() {

        return contentType_;
    }

    public void setContentType(String contentType) {

        contentType_ = contentType;
    }
}
