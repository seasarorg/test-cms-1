package org.seasar.cms.ymir.creator;

import org.seasar.framework.convention.NamingConvention;

public class PageCreator extends
        org.seasar.framework.container.creator.PageCreator {

    public PageCreator(NamingConvention namingConvention) {
        super(namingConvention);
        setExternalBinding(false);
    }
}
