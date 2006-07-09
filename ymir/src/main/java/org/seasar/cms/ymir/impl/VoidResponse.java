package org.seasar.cms.ymir.impl;


public class VoidResponse extends ResponseBase {

    public static final VoidResponse INSTANCE = new VoidResponse();

    public int getType() {

        return TYPE_VOID;
    }
}
