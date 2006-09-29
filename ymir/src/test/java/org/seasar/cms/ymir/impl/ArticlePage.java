package org.seasar.cms.ymir.impl;

public class ArticlePage {

    private int counter_;

    public void _render() {
        counter_++;
    }

    public int getCounter() {
        return counter_;
    }
}
