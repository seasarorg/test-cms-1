package org.seasar.cms.ymir.extension.creator.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.seasar.cms.ymir.extension.creator.Template;

public class FileTemplate implements Template {

    private String path_;

    private File file_;

    public FileTemplate(String path, File file) {
        path_ = path;
        file_ = file;
    }

    public boolean exists() {
        return file_.exists();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file_);
    }

    public String getPath() {
        return path_;
    }

    public long lastModified() {
        return file_.lastModified();
    }

    public String getName() {
        return file_.getName();
    }

    public OutputStream getOutputStream() throws IOException {

        file_.getParentFile().mkdirs();
        return new FileOutputStream(file_);
    }
}
