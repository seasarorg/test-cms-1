package org.seasar.cms.pluggable.impl;

import org.seasar.cms.pluggable.PluggableNamingConvention;
import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.seasar.framework.util.Class24Util;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

@SuppressWarnings("unchecked")
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

    // S2.4の元々の実装に対して、APageクラスがAPageではなくaPageと変換されるように
    // 変更している。
    // これはなんらかの名前（例えばリクエストパス）にサフィックスを連結させてできる
    // タイプのコンポーネント名と整合性をとるためである。
    @Override
    public String fromClassNameToShortComponentName(String className) {
        if (StringUtil.isEmpty(className)) {
            throw new EmptyRuntimeException("className");
        }
        className = Class24Util.getShortClassName(className);
        String implementationSuffix = getImplementationSuffix();
        if (className.endsWith(implementationSuffix)) {
            className = className.substring(0, className.length()
                    - implementationSuffix.length());
        }
        String suffix = fromClassNameToSuffix(className);
        if (suffix == null) {
            return StringUtil.decapitalize(className);
        } else {
            return StringUtil.decapitalize(className.substring(0, className
                    .length()
                    - suffix.length()))
                    + suffix;
        }
    }

    // S2.4の元々の実装に対して、APageクラスがAPageではなくaPageと変換されるように
    // 変更している。
    // これはなんらかの名前（例えばリクエストパス）にサフィックスを連結させてできる
    // タイプのコンポーネント名と整合性をとるためである。
    public String fromClassNameToComponentName(String className) {
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
                buf.append(
                        StringUtil.decapitalize(array[i].substring(0, array[i]
                                .length()
                                - suffix.length()))).append(suffix);
            } else {
                buf.append(array[i]);
                buf.append('_');
            }
        }
        return buf.toString();
    }

    public String fromClassNameToSuffix(String className) {
        if (StringUtil.isEmpty(className)) {
            throw new EmptyRuntimeException("className");
        }
        for (int i = className.length() - 1; i >= 0
                && className.charAt(i) != '.'; --i) {
            if (Character.isUpperCase(className.charAt(i))) {
                return className.substring(i);
            }
        }
        return null;
    }
}
