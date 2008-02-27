package org.seasar.cms.classbuilder.util;

import java.lang.reflect.Method;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;

public class ClassBuilderUtils {
    protected ClassBuilderUtils() {
    }

    public static String toComponentName(String name) {
        if (name.length() > 0) {
            // FIXME 正しいルールに置き換えよう。
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    public static S2ContainerPreparer getPreparer(ComponentDef componentDef) {
        S2Container container = componentDef.getContainer();
        ComponentDef[] componentDefs = ContainerUtils.findLocalComponentDefs(
                container, S2ContainerPreparer.class);
        if (componentDefs.length == 0) {
            return null;
        }

        return (S2ContainerPreparer) componentDefs[0].getComponent();
    }

    public static Method findMethod(Class<?> clazz, String componentName,
            String prefix) {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.startsWith(prefix)
                    && componentName.equals(toComponentName(methodName
                            .substring(prefix.length())))) {
                return methods[i];
            }
        }
        return null;
    }
}
