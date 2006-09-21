package org.seasar.cms.ymir.extension.creator.impl;

public class Merge3Base extends Merge3BaseBase {

    public void setHoe(String hoe) {
        hoe_ = hoe;
    }

    public void setFuga(String fuga) {
        fuga_ = fuga;
    }

    public int times(int f1, int f2) {
        return f1 * f2;
    }

    public int div(int f1, int f2) {
        return f1 / f2;
    }

    public int inc(int f1, int f2) {
        return f1 + 1;
    }
}
