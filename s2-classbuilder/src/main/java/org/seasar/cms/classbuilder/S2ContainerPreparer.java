package org.seasar.cms.classbuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.seasar.cms.classbuilder.annotation.ManualBindingProperties;
import org.seasar.cms.classbuilder.util.S2ContainerPreparerUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;


public class S2ContainerPreparer
{
    public static final String METHODPREFIX_DEFINE = "define";

    public static final String METHODPREFIX_REDEFINE = "redefine";

    public static final String METHODPREFIX_NEW = "new";

    private S2Container container_;


    public final void setContainer(S2Container container)
    {
        container_ = container;
    }


    public final S2Container getContainer()
    {
        return container_;
    }


    public void include()
    {
    }


    @SuppressWarnings("unchecked")
    public final <T> T getComponent(Class<T> key)
    {
        return (T)container_.getComponent(key);
    }


    public final Object getComponent(Object key)
    {
        return container_.getComponent(key);
    }


    public final void include(Class<? extends S2ContainerPreparer> preparer)
    {
        include(preparer.getName().replace('.', '/').concat(".class"));
    }


    public final void include(String path)
    {
        S2ContainerFactory.include(container_, path);
    }


    ClassLoader getClassLoader()
    {
        ClassLoader classLoader = Thread.currentThread()
            .getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader;
    }


    public final void initialize(Object component, String name)
    {
        Method method = getDefineMethod(name);
        if (method != null) {
            try {
                method.invoke(this, new Object[] { component });
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Can't initialize: " + name);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Can't initialize: " + name);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException("Can't initialize: " + name);
            }
        }
    }


    Method getDefineMethod(String name)
    {
        return findMethod(name, METHODPREFIX_DEFINE);
    }


    Method findMethod(String name, String prefix)
    {
        Method[] methods = getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.startsWith(prefix)
                && name.equals(S2ContainerPreparerUtils
                    .toComponentName(methodName.substring(prefix.length())))) {
                return methods[i];
            }
        }
        return null;
    }


    public final String[] getManualBindingProperties(String name)
    {
        Method method = getDefineMethod(name);
        if (method != null) {
            ManualBindingProperties annotation = method
                .getAnnotation(ManualBindingProperties.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        return new String[0];
    }


    public final Object newInstance(String name)
    {
        Method method = findMethod(name, METHODPREFIX_NEW);
        if (method != null) {
            try {
                return method.invoke(this, new Object[0]);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(
                    "Can't invoke method for instanciating component: "
                        + method.getName(), ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(
                    "Can't invoke method for instanciating component: "
                        + method.getName(), ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(
                    "Can't invoke method for instanciating component: "
                        + method.getName(), ex);
            }
        } else {
            return null;
        }
    }
}
