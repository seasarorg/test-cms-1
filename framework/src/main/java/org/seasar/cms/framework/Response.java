package org.seasar.cms.framework;

import java.io.InputStream;

public interface Response {

    int TYPE_PASSTHROUGH = 0;

    int TYPE_FORWARD = 1;

    int TYPE_REDIRECT = 2;

    int TYPE_SELF_CONTAINED = 3;

    int TYPE_STATUS = 4;

    int TYPE_VOID = 5;

    int getType();

    void setType(int type);

    String getPath();

    void setPath(String path);

    InputStream getInputStream();

    void setInputStream(InputStream is);

    String getContentType();

    void setContentType(String contentType);

    int getStatus();

    void setStatus(int status);
}
