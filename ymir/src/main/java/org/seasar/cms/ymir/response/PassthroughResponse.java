package org.seasar.cms.ymir.response;


public class PassthroughResponse extends ResponseBase {

    public static final PassthroughResponse INSTANCE = new PassthroughResponse();

    public String toString() {

        return "(passthrough)";
    }

    public int getType() {

        return TYPE_PASSTHROUGH;
    }
}
