package org.seasar.cms.mailsender;

import java.util.Date;

public class HoeDto {
    private String message;

    private Date date;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
