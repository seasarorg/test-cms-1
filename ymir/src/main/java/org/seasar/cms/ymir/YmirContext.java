package org.seasar.cms.ymir;

public class YmirContext {

    private static Ymir ymir_;

    public static void setYmir(Ymir ymir) {

        ymir_ = ymir;
    }

    public static Ymir getYmir() {

        return ymir_;
    }
}
