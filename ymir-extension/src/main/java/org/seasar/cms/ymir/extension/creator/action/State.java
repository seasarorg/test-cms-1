package org.seasar.cms.ymir.extension.creator.action;

public enum State {

    FALSE, TRUE, ANY;

    public static State valueOf(boolean b) {
        if (b) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
}
