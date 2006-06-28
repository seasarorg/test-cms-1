package org.seasar.cms.framework.creator;

import java.util.HashMap;
import java.util.Map;

public class BodyDesc implements Cloneable {

    private static final String KEY_BODY = "body";

    private static final String KEY_ASIS = "asIs";

    private String key_;

    private Object root_;

    public BodyDesc(String key, Object root) {

        key_ = key;
        root_ = root;
    }

    public BodyDesc(String body) {

        key_ = KEY_ASIS;
        Map root = new HashMap();
        root.put(KEY_BODY, body);
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
