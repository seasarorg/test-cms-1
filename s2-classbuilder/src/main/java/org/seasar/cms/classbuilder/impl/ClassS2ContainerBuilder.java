package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.Method;

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
        String className = path.substring(0, path.length() - SUFFIX.length())
            .replace('/', '.');
        ClassLoader classLoader = Thread.currentThread()
            .getContextClassLoader();
        Class clazz;
        try {
            // S2Container関連のクラスがコンテキストクラスローダから見えない場合があるためこうしている。
            clazz = Class
                .forName(className, true, new CompositeClassLoader(
                    new ClassLoader[] { classLoader,
                        getClass().getClassLoader() }));
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Class not found: " + className, ex);
        }
        if (S2ContainerPreparer.class.isAssignableFrom(clazz)) {
            return (Class<? extends S2ContainerPreparer>)clazz;
        } else {
            throw new RuntimeException("Not Preparer: " + path);
        }
    }


    public S2Container include(S2Container parent, String path)
    {
        S2Container child = build(parent, path);
        parent.include(child);
        return child;
    }
}
