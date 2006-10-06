package org.seasar.cms.ymir.extension.creator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Template {

    boolean exists();

    InputStream getInputStream() throws IOException;

    String getPath();

    long lastModified();

    String getName();

    OutputStream getOutputStream() throws IOException;
}
