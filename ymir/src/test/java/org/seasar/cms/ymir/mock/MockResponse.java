package org.seasar.cms.ymir.mock;

import java.io.InputStream;

import org.seasar.cms.ymir.Response;

public class MockResponse implements Response {

    public String getContentType() {
        return null;
    }

    public InputStream getInputStream() {
        return null;
    }

    public String getPath() {
        return null;
    }

    public int getStatus() {
        return 0;
    }

    public int getType() {
        return 0;
    }

    public void setContentType(String contentType) {
    }

    public void setInputStream(InputStream is) {
    }

    public void setPath(String path) {
    }

    public void setStatus(int status) {
    }

    public void setType(int type) {
    }
}
