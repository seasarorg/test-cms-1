package org.seasar.cms.framework.beanutils;

import org.seasar.cms.framework.FormFile;

public class TestBean {

    private FormFile file_;

    private FormFile[] files_;

    public FormFile getFile() {
        return file_;
    }

    public void setFile(FormFile file) {
        file_ = file;
    }

    public FormFile[] getFiles() {
        return files_;
    }

    public void setFiles(FormFile[] files) {
        files_ = files;
    }
}
