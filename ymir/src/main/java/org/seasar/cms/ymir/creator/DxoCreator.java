package org.seasar.cms.ymir.creator;

import org.seasar.framework.container.hotdeploy.creator.DxoOndemandCreator;
import org.seasar.framework.convention.NamingConvention;

public class DxoCreator extends DxoOndemandCreator {

    public DxoCreator(NamingConvention namingConvention) {
        super(namingConvention);
    }

    protected String[] composeClassNames(String rootPackageName,
            String componentName) {
        return CreatorUtils.composeClassNames(getPackageNames(rootPackageName),
                componentName);
    }
}
