package org.seasar.cms.framework;

public class PageNotFoundException extends Exception {

    private static final long serialVersionUID = 4300901393295361786L;

    private String path_;

    public PageNotFoundException(String path) {

        path_ = path;
    }

    public String toString() {

        return "Page not found: " + path_;
    }

    public String getPath() {

        return path_;
    }
}
