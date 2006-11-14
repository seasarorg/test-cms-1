package org.seasar.cms.ymir.beanutils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;

public class MockFileItem implements FileItem {

    private static final long serialVersionUID = -728161807594247921L;

    public InputStream getInputStream() throws IOException {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isInMemory() {
        return false;
    }

    public long getSize() {
        return 0;
    }

    public byte[] get() {
        return null;
    }

    public String getString(String encoding)
            throws UnsupportedEncodingException {
        return null;
    }

    public String getString() {
        return null;
    }

    public void write(File file) throws Exception {
    }

    public void delete() {
    }

    public String getFieldName() {
        return null;
    }

    public void setFieldName(String name) {
    }

    public boolean isFormField() {
        return false;
    }

    public void setFormField(boolean state) {
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
