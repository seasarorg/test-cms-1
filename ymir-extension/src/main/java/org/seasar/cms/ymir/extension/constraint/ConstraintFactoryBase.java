package org.seasar.cms.ymir.extension.constraint;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

abstract public class ConstraintFactoryBase {

    private static final String PREFIX_SET = "set";

    private static final String PREFIX_GET = "get";

    private static final String PREFIX_IS = "is";

    protected String getPropertyName(AnnotatedElement element) {
        if (!(element instanceof Method)) {
            return null;
        }
        return toPropertyName(((Method) element).getName());
    }

    String toPropertyName(String methodName) {

        String name;
        if (methodName == null) {
            return null;
        } else if (methodName.startsWith(PREFIX_IS)) {
            name = methodName.substring(PREFIX_IS.length());
        } else if (methodName.startsWith(PREFIX_GET)) {
            name = methodName.substring(PREFIX_GET.length());
        } else if (methodName.startsWith(PREFIX_SET)) {
            name = methodName.substring(PREFIX_SET.length());
        } else {
            return null;
        }
        if (name.length() == 0) {
            return null;
        } else if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
            return name;
        } else {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
    }

    String[] add(String[] strings, String string) {

        if (string != null) {
            String[] newStrings = new String[strings.length + 1];
            System.arraycopy(strings, 0, newStrings, 0, strings.length);
            newStrings[strings.length] = string;
            return newStrings;
        } else {
            return strings;
        }
    }

    String[] add(String[] strings, String[] strings2, String string) {

        String[] newStrings = new String[strings.length + strings2.length
                + (string != null ? 1 : 0)];
        System.arraycopy(strings, 0, newStrings, 0, strings.length);
        System.arraycopy(strings2, 0, newStrings, strings.length,
                strings2.length);
        if (string != null) {
            newStrings[strings.length + strings2.length] = string;
        }
        return newStrings;
    }
}