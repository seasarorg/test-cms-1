package org.seasar.cms.ymir.creator;

import org.seasar.framework.util.StringUtil;

public class CreatorUtils {

    public static String composeClassName(String rootPackageName,
            String middlePackageName, String componentName) {
        return concatName(
                concatName(concatName(new StringBuffer(100), rootPackageName),
                        middlePackageName),
                StringUtil.capitalize(componentName)).toString();
    }

    static StringBuffer concatName(StringBuffer sb, String name) {
        if (name != null) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(name);
        }
        return sb;
    }

    public static String[] composeClassNames(String[] packageNames,
            String componentName) {
        String shortClassName = StringUtil.capitalize(componentName);
        String[] classNames = new String[packageNames.length];
        for (int i = 0; i < packageNames.length; ++i) {
            classNames[i] = concatName(
                    concatName(new StringBuffer(100), packageNames[i]),
                    shortClassName).toString();
        }
        return classNames;
    }
}
