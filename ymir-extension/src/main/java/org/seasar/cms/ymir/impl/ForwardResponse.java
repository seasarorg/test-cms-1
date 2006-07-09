package org.seasar.cms.ymir.impl;

public class ForwardResponse extends TransitionResponse {

    public ForwardResponse() {
    }

    public ForwardResponse(String path) {

        super(path);
    }

    public int getType() {

        return TYPE_FORWARD;
    }
}
