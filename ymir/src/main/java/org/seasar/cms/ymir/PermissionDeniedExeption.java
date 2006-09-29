package org.seasar.cms.ymir;

public class PermissionDeniedExeption extends Exception {

    private static final long serialVersionUID = -7495239316902981080L;

    private String path_;

    public PermissionDeniedExeption(String path) {

        path_ = path;
    }

    public String toString() {

        return "Current user has no permission to access: " + path_;
    }

    public String getPath() {

        return path_;
    }
}
