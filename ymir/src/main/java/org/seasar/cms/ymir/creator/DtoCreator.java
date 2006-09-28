package org.seasar.cms.ymir.creator;

import org.seasar.framework.container.hotdeploy.creator.DtoOndemandCreator;
import org.seasar.framework.convention.NamingConvention;

public class DtoCreator extends DtoOndemandCreator {

    public DtoCreator(NamingConvention namingConvention) {
        super(namingConvention);
    }

    protected String composeClassName(String rootPackageName,
            String componentName) {
        return CreatorUtils.composeClassName(rootPackageName,
                getMiddlePackageName(), componentName);
    }
}
