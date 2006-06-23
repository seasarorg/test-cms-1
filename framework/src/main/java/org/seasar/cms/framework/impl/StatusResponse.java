package org.seasar.cms.framework.impl;

public class StatusResponse extends ResponseBase {

    private int status_;

    public StatusResponse() {
    }

    public StatusResponse(int status) {

        setStatus(status);
    }

    public int getType() {

        return TYPE_STATUS;
    }

    public int getStatus() {

        return status_;
    }

    public void setStatus(int status) {

        status_ = status;
    }
}
