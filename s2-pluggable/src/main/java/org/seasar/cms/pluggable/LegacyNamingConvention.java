package org.seasar.cms.pluggable;

import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

public class LegacyNamingConvention extends NamingConventionImpl {
    private static final String IMPLEMENTATION_SUFFIX = "Impl";

    public String fromClassNameToComponentName(String className) {
        if (className == null) {
            throw new EmptyRuntimeException("className");
        }
        String s = StringUtil.decapitalize(ClassUtil
            .getShortClassName(className));
        if (s.endsWith(IMPLEMENTATION_SUFFIX)) {
            return s.substring(0, s.length() - IMPLEMENTATION_SUFFIX.length());
        }
        return s;
    }
}
