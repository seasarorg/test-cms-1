package org.seasar.cms.framework.impl;

import org.seasar.cms.framework.Response;

public class ResponseImpl implements Response {

    private boolean redirect_;

    private String path_;

    public ResponseImpl() {
    }

    public ResponseImpl(String path, boolean redirect) {

        path_ = path;
        redirect_ = redirect;
    }

    public boolean isRedirect() {

        return redirect_;
    }

    public void setRedirect(boolean redirect) {

        redirect_ = redirect;
    }

    public String getPath() {

        return path_;
    }

    public void setPath(String path) {

        path_ = path;
    }
}
