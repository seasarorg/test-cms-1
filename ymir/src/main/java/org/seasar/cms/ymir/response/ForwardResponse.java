package org.seasar.cms.ymir.response;


public class ForwardResponse extends TransitionResponse {

    public ForwardResponse() {
    }

    public ForwardResponse(String path) {

        super(path);
    }

    public String toString() {

        return "forward:" + getPath();
    }

    public int getType() {

        return TYPE_FORWARD;
    }
}
