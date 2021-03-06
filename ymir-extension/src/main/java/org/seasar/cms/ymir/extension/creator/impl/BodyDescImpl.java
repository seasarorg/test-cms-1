package org.seasar.cms.ymir.extension.creator.impl;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.extension.creator.BodyDesc;

public class BodyDescImpl implements BodyDesc {

    private static final String KEY_ASIS = "asIs";

    private static final String PROP_BODY = "body";

    private String key_;

    private Object root_;

    public BodyDescImpl(String key, Object root) {

        key_ = key;
        root_ = root;
    }

    public BodyDescImpl(String body) {

        key_ = KEY_ASIS;
        Map<String, String> root = new HashMap<String, String>();
        root.put(PROP_BODY, body);
        root_ = root;
    }

    public Object clone() {

        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getKey() {
        return key_;
    }

    public void setKey(String key) {
        key_ = key;
    }

    public Object getRoot() {
        return root_;
    }

    public void setRoot(Object root) {
        root_ = root;
    }
}
