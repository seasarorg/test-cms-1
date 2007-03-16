package com.example.web;

import org.seasar.cms.ymir.Note;

public class IndexPage extends IndexPageBase {

    protected String param1_;

    protected Integer[] param2_;

    protected Note[] results_;

    public String getParam1() {
        return param1_;
    }

    public void setParam2(Integer[] param2) {
        param2_ = param2;
    }

    public Note[] getResults() {
        return results_;
    }

    public void setResult(Note[] results) {
        results_ = results;
    }

    public void _render() {
    }
}
