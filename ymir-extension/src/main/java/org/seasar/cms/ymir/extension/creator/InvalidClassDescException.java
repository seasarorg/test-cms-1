package org.seasar.cms.ymir.extension.creator;

public class InvalidClassDescException extends Exception {

    private static final long serialVersionUID = 7798729644594038468L;

    private String[] lackingClassNames_;

    public InvalidClassDescException(String[] lackingClassNames) {

        lackingClassNames_ = lackingClassNames;
    }

    public String[] getLackingClassNames() {

        return lackingClassNames_;
    }
}
