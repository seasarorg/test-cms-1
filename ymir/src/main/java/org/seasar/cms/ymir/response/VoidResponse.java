package org.seasar.cms.ymir.response;



public class VoidResponse extends ResponseBase {

    public static final VoidResponse INSTANCE = new VoidResponse();

    public int getType() {

        return TYPE_VOID;
    }
}
