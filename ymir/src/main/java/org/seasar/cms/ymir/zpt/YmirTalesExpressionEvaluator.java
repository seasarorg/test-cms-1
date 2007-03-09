package org.seasar.cms.ymir.zpt;

import net.skirnir.freyja.zpt.webapp.ServletTalesExpressionEvaluator;

public class YmirTalesExpressionEvaluator extends
        ServletTalesExpressionEvaluator {

    public YmirTalesExpressionEvaluator() {

        addPathResolver(new YmirPathResolver());
    }
}
