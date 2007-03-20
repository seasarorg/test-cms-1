package org.seasar.cms.ymir.convention;

import org.seasar.cms.pluggable.impl.PluggableNamingConventionImpl;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.seasar.framework.util.StringUtil;

public class YmirNamingConvention extends PluggableNamingConventionImpl {

    private static final char PACKAGE_SEPARATOR = '_';

    public static final String SUFFIX_EXCEPTIONHANDLER = "Handler";

    public String fromClassNameToComponentName(final String className) {
        if (StringUtil.isEmpty(className)) {
            throw new EmptyRuntimeException("className");
        }
        String cname = toInterfaceClassName(className);
        String suffix = fromClassNameToSuffix(cname);
        String middlePackageName = fromSuffixToPackageName(suffix);
        String key = "." + middlePackageName + ".";
        int index = cname.indexOf(key);
        String name = null;
        if (index > 0) {
            name = cname.substring(index + key.length());
        } else {
            key = "." + getSubApplicationRootPackageName() + ".";
            index = cname.indexOf(key);
            if (index < 0) {
                return fromClassNameToShortComponentName(className);
            }
            name = cname.substring(index + key.length());
        }
        String[] array = StringUtil.split(name, ".");
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            if (i == array.length - 1) {
                buf.append(StringUtil.decapitalize(array[i]));
            } else {
                buf.append(array[i]);
                buf.append('_');
            }
        }
        return buf.toString();
    }

    public String fromComponentNameToPartOfClassName(String componentName) {
        String partOfClassName = super
                .fromComponentNameToPartOfClassName(componentName);
        if (componentName.startsWith("_")) {
            return "_" + partOfClassName;
        } else {
            return partOfClassName;
        }
    }
}
