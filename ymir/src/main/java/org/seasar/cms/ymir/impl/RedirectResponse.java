package org.seasar.cms.ymir.impl;

public class RedirectResponse extends TransitionResponse {

    public RedirectResponse() {
    }

    public RedirectResponse(String path) {

        super(path);
    }

    public String toString() {

        return "redirect:" + getPath();
    }

    public int getType() {

        return TYPE_REDIRECT;
    }
}
