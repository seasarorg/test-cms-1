package org.seasar.cms.ymir;

import org.seasar.cms.ymir.util.TokenUtils;

public class Token {
    private String name_;

    private String value_;

    public Token(String value) {
        this(TokenUtils.KEY_TOKEN, value);
    }

    public Token(String name, String value) {
        name_ = name;
        value_ = value;
    }

    public String getName() {
        return name_;
    }

    public String getValue() {
        return value_;
    };
}
