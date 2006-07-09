package org.seasar.cms.ymir.impl;

public class RedirectResponse extends TransitionResponse {

    public RedirectResponse() {
    }

    public RedirectResponse(String path) {

        super(path);
    }

    public int getType() {

        return TYPE_REDIRECT;
    }
}
