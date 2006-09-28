package org.seasar.cms.ymir.creator;

import org.seasar.framework.container.hotdeploy.creator.PageOndemandCreator;
import org.seasar.framework.convention.NamingConvention;

public class PageCreator extends PageOndemandCreator {

    public PageCreator(NamingConvention namingConvention) {
        super(namingConvention);
        setExternalBinding(false);
    }

    protected String composeClassName(String rootPackageName,
            String componentName) {
        return CreatorUtils.composeClassName(rootPackageName,
                getMiddlePackageName(), componentName);
    }
}
