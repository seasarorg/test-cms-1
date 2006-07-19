package org.seasar.cms.ymir.convention;

import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

public class YmirNamingConvention extends NamingConventionImpl {

    private static final char PACKAGE_SEPARATOR = '_';

    public String fromClassNameToComponentName(String className) {
        if (className == null) {
            throw new EmptyRuntimeException("className");
        }
        String wwwkey = "." + getWebPackageName() + ".";
        int index = className.lastIndexOf(wwwkey);
        if (index > 0) {
            String s = className.substring(index + wwwkey.length());
            if (s.endsWith(getImplementationSuffix())) {
                String implkey = "." + getImplementationPackageName() + ".";
                index = s.lastIndexOf(implkey);
                if (index < 0) {
                    throw new IllegalArgumentException(className);
                }
                String packageName = s.substring(0, index).replace('.',
                    PACKAGE_SEPARATOR);
                String name = StringUtil.decapitalize(s.substring(index
                    + implkey.length(), s.length()
                    - getImplementationSuffix().length()));
                return packageName + PACKAGE_SEPARATOR + name;
            }
            index = s.lastIndexOf('.');
            if (index < 0) {
                return StringUtil.decapitalize(s);
            } else {
                String packageName = s.substring(0, index).replace('.',
                    PACKAGE_SEPARATOR);
                String name = StringUtil.decapitalize(s.substring(index + 1));
                return packageName + PACKAGE_SEPARATOR + name;
            }
        }
        String s = StringUtil.decapitalize(ClassUtil
            .getShortClassName(className));
        if (s.endsWith(getImplementationSuffix())) {
            return s.substring(0, s.length()
                - getImplementationSuffix().length());
        }
        return s;
    }
}
