package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.annotation.Component;
import org.seasar.cms.classbuilder.util.CompositeClassLoader;
import org.seasar.cms.classbuilder.util.S2ContainerPreparerUtils;
import org.seasar.framework.container.AspectDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.annotation.tiger.Aspect;
import org.seasar.framework.container.assembler.AutoBindingDefFactory;
import org.seasar.framework.container.deployer.InstanceDefFactory;
import org.seasar.framework.container.factory.AbstractS2ContainerBuilder;
import org.seasar.framework.container.factory.AnnotationHandler;
import org.seasar.framework.container.factory.AnnotationHandlerFactory;
import org.seasar.framework.container.factory.AspectDefFactory;
import org.seasar.framework.container.impl.S2ContainerImpl;


public class ClassS2ContainerBuilder extends AbstractS2ContainerBuilder
{
    public static final String SUFFIX = ".class";

    private static final String JAR_SUFFIX = ".jar!/";


    public S2Container build(String path)
    {
        return build(null, path);
    }


    S2Container build(S2Container parent, String path)
    {
        S2Container container = createContainer(parent, path);
        Class<? extends S2ContainerPreparer> preparerClass = getPreparerClass(path);

        S2ContainerPreparer preparer;
        try {
            preparer = preparerClass.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Can't instanciate Preparer: " + path,
                ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Can't instanciate Preparer: " + path,
                ex);
        }
        container.register(preparer);
        preparer.setContainer(container);
        preparer.include();
        registerComponentDefs(container, preparerClass);

        return container;
    }


    S2Container createContainer(S2Container parent, String path)
    {
        S2Container container = new S2ContainerImpl();
        container.setPath(path);
        if (parent != null) {
            container.setRoot(parent.getRoot());
        }
        return container;
    }


    void registerComponentDefs(S2Container container,
        Class<? extends S2ContainerPreparer> preparerClass)
    {
        Method[] methods = preparerClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            if (name.startsWith(S2ContainerPreparer.METHODPREFIX_DEFINE)) {
                defineComponentDef(container, methods[i]);
            } else if (name
                .startsWith(S2ContainerPreparer.METHODPREFIX_REDEFINE)) {
                redefineComponentDef(methods[i]);
            }
        }
    }


    void redefineComponentDef(Method method)
    {
    }


    void defineComponentDef(S2Container container, Method method)
    {
        AnnotationHandler annoHandler = AnnotationHandlerFactory
            .getAnnotationHandler();

        String componentName = S2ContainerPreparerUtils.toComponentName(method
            .getName().substring(
                S2ContainerPreparer.METHODPREFIX_DEFINE.length()));
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            throw new RuntimeException(
                "Definition method must have at least one parameter but "
                    + parameterTypes.length + ": " + method.getName());
        }
        Class componentClass = parameterTypes[0];
        Class[] constructorParameterTypes = new Class[parameterTypes.length - 1];
        System.arraycopy(parameterTypes, 1, constructorParameterTypes, 0,
            constructorParameterTypes.length);

        ComponentDef componentDef = annoHandler.createComponentDef(
            componentClass, null);
        componentDef.setComponentName(componentName);
        annoHandler.appendDI(componentDef);
        annoHandler.appendAspect(componentDef);
        annoHandler.appendInterType(componentDef);

        Component componentAnnotation = method.getAnnotation(Component.class);
        if (componentAnnotation != null) {
            componentDef.setInstanceDef(InstanceDefFactory
                .getInstanceDef(componentAnnotation.instance().getName()));
            componentDef
                .setAutoBindingDef(AutoBindingDefFactory
                    .getAutoBindingDef(componentAnnotation.autoBinding()
                        .getName()));
            componentDef.setExternalBinding(componentAnnotation
                .externalBinding());
        }

        Aspect aspectAnnotation = method.getAnnotation(Aspect.class);
        if (aspectAnnotation != null) {
            AspectDef aspectDef = AspectDefFactory.createAspectDef(
                aspectAnnotation.value(), aspectAnnotation.pointcut());
            componentDef.addAspectDef(aspectDef);
        }

        annoHandler.appendInitMethod(componentDef);
        annoHandler.appendDestroyMethod(componentDef);

        container.register(componentDef);
    }


    Class<? extends S2ContainerPreparer> getPreparerClass(String path)
    {
        ClassLoader classLoader = getClassLoaderForLoadingPreparer();
        Class clazz;
        if (path.indexOf(':') < 0) {
            clazz = getClassFromClassName(path, classLoader);
        } else {
            clazz = getClassFromURL(path, classLoader);
        }
        if (clazz == null) {
            throw new RuntimeException("Class not found: " + path);
        }
        if (S2ContainerPreparer.class.isAssignableFrom(clazz)) {
            return (Class<? extends S2ContainerPreparer>)clazz;
        } else {
            throw new RuntimeException("Not Preparer: " + path);
        }
    }


    Class getClassFromURL(String path, ClassLoader classLoader)
    {
        String[] classNames;
        int jarSuffix = path.indexOf(JAR_SUFFIX);
        if (jarSuffix >= 0) {
            // Jar。
            classNames = new String[] { path.substring(
                jarSuffix + JAR_SUFFIX.length(),
                path.length() - SUFFIX.length()).replace('/', '.') };
        } else {
            path = path.substring(path.indexOf(':') + 1,
                path.length() - SUFFIX.length()).replace('/', '.');
            List<String> classNameList = new ArrayList<String>();
            int len = path.length();
            for (int i = len - 1; i >= 0; i--) {
                if (path.charAt(i) == '.') {
                    classNameList.add(path.substring(i + 1));
                }
            }
            classNames = classNameList.toArray(new String[0]);
        }
        for (int i = 0; i < classNames.length; i++) {
            try {
                return Class.forName(classNames[i], true, classLoader);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return null;
    }


    Class getClassFromClassName(String path, ClassLoader classLoader)
    {
        String className = path.substring(0, path.length() - SUFFIX.length())
            .replace('/', '.');
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }


    ClassLoader getClassLoaderForLoadingPreparer()
    {
        ClassLoader classLoader = Thread.currentThread()
            .getContextClassLoader();
        // S2Container関連のクラスがコンテキストクラスローダから見えない場合に備えてこうしている。
        return new CompositeClassLoader(new ClassLoader[] { classLoader,
            getClass().getClassLoader() });
    }


    public S2Container include(S2Container parent, String path)
    {
        S2Container child = build(parent, path);
        parent.include(child);
        return child;
    }
}
