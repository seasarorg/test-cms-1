package org.seasar.cms.framework.impl;

import java.io.InputStream;

import org.seasar.cms.framework.Response;

public class VoidResponse implements Response {

    public static final VoidResponse INSTANCE = new VoidResponse();

    public int getType() {

        return TYPE_VOID;
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

        return null;
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
}
