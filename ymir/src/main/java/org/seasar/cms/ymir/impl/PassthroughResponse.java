package org.seasar.cms.ymir.impl;

public class PassthroughResponse extends ResponseBase {

    public static final PassthroughResponse INSTANCE = new PassthroughResponse();

    public int getType() {

        return TYPE_PASSTHROUGH;
    }
}
