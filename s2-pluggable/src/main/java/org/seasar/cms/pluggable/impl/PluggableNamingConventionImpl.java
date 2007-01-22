package org.seasar.cms.pluggable.impl;

import org.seasar.cms.pluggable.PluggableNamingConvention;
import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

public class PluggableNamingConventionImpl extends NamingConventionImpl
        implements PluggableNamingConvention {

    public String fromComponentNameToClassName(String componentName) {
        if (StringUtil.isEmpty(componentName)) {
            throw new EmptyRuntimeException("componentName");
        }
        String suffix = fromComponentNameToSuffix(componentName);
        if (suffix == null) {
            return null;
        }
        String middlePackageName = fromSuffixToPackageName(suffix);
        String partOfClassName = fromComponentNameToPartOfClassName(componentName);
        String[] rootPackageNames = getRootPackageNames();
        boolean subAppSuffix = isSubApplicationSuffix(suffix);
        for (int i = 0; i < rootPackageNames.length; ++i) {
            String rootPackageName = rootPackageNames[i];
            if (subAppSuffix) {
                return concatClassName(rootPackageName,
                        getSubApplicationRootPackageName(), partOfClassName);
            } else {
                return concatClassName(rootPackageName, middlePackageName,
                        partOfClassName);
            }
        }
        return null;
    }

    protected String concatClassName(String rootPackageName,
            String middlePackageName, String partOfClassName) {
        return ClassUtil.concatName(rootPackageName, ClassUtil.concatName(
                middlePackageName, partOfClassName));
    }
}
