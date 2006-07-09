package org.seasar.cms.ymir.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.seasar.cms.ymir.FormFile;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class FormFileImpl implements FormFile {
    private FileItem fileItem_;

    public FormFileImpl(FileItem fileItem) {
        fileItem_ = fileItem;
    }

    /*
     * FormFile
     */

    public byte[] get() {
        return fileItem_.get();
    }

    public String getContentType() {
        return fileItem_.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        return fileItem_.getInputStream();
    }

    public String getName() {
        return fileItem_.getName();
    }

    public long getSize() {
        return fileItem_.getSize();
    }

    public String getString() {
        return fileItem_.getString();
    }

    public String getString(String encoding)
        throws UnsupportedEncodingException {
        return fileItem_.getString(encoding);
    }

    /*
     * Object
     */

    public String toString() {
        return getName();
    }
}
