package org.seasar.cms.ymir.creator;

import org.seasar.framework.container.hotdeploy.creator.DaoOndemandCreator;
import org.seasar.framework.convention.NamingConvention;

public class DaoCreator extends DaoOndemandCreator {

    public DaoCreator(NamingConvention namingConvention) {
        super(namingConvention);
    }

    protected String composeClassName(String rootPackageName,
            String componentName) {
        return CreatorUtils.composeClassName(rootPackageName,
                getMiddlePackageName(), componentName);
    }
}
