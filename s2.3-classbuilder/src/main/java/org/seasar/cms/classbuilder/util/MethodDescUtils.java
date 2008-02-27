package org.seasar.cms.classbuilder.util;

import java.lang.reflect.Method;

import org.seasar.cms.classbuilder.PreparerMethodDef;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.container.MethodDef;
import org.seasar.framework.container.util.BindingUtil;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;

public class MethodDescUtils {
    private static Method getSuitableMethod(Method[] methods) {
        int argSize = -1;
        Method method = null;
        for (int i = 0; i < methods.length; ++i) {
            int tempArgSize = methods[i].getParameterTypes().length;
            if (tempArgSize > argSize
                    && BindingUtil.isAutoBindable(methods[i]
                            .getParameterTypes())) {
                method = methods[i];
                argSize = tempArgSize;
            }
        }
        return method;
    }

    public static String serializeMethod(Method method) {
        if (method == null) {
            return null;
        }
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }

    public static Method deserializeMethod(MethodDef methodDef) {
        return deserializeMethod(methodDef.getMethodName(), methodDef
                .getContainer().getClassLoader());
    }

    public static Method deserializeMethod(String serialized,
            ClassLoader classLoader) {
        if (serialized == null) {
            return null;
        }

        int sharp = serialized.indexOf('#');
        String className = serialized.substring(0, sharp);
        String methodName = serialized.substring(sharp + 1);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            Class clazz;
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException ex) {
                throw new ClassNotFoundRuntimeException(ex);
            }
            BeanDesc beanDesc = BeanDescFactory.getBeanDesc(clazz);
            Method[] methods = beanDesc.getMethods(methodName);
            return getSuitableMethod(methods);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public static Method getMethod(BeanDesc beanDesc, MethodDef methodDef) {
        if (methodDef instanceof PreparerMethodDef) {
            return ((PreparerMethodDef) methodDef).getMethod();
        } else {
            String methodName = methodDef.getMethodName();
            if (methodName != null) {
                Method[] methods = beanDesc.getMethods(methodName);
                return getSuitableMethod(methods);
            } else {
                return null;
            }
        }
    }
}
