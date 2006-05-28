package org.seasar.cms.framework;

public interface Response {

    boolean isRedirect();

    void setRedirect(boolean redirect);

    String getPath();

    void setPath(String path);
}
