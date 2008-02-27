package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.annotation.Component;
import org.seasar.cms.classbuilder.annotation.Dicon;
import org.seasar.cms.classbuilder.util.ClassBuilderUtils;
import org.seasar.cms.classbuilder.util.CompositeClassLoader;
import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.ArgDef;
import org.seasar.framework.container.AspectDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.DestroyMethodDef;
import org.seasar.framework.container.InitMethodDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.annotation.tiger.Aspect;
import org.seasar.framework.container.assembler.AutoBindingDefFactory;
import org.seasar.framework.container.deployer.InstanceDefFactory;
import org.seasar.framework.container.factory.AbstractS2ContainerBuilder;
import org.seasar.framework.container.factory.AnnotationHandler;
import org.seasar.framework.container.factory.AnnotationHandlerFactory;
import org.seasar.framework.container.factory.AspectDefFactory;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.impl.ArgDefImpl;
import org.seasar.framework.container.impl.S2ContainerImpl;

public class ClassS2ContainerBuilder extends AbstractS2ContainerBuilder {
    public static final String METHODPREFIX_DEFINE = "define";

    public static final String METHODPREFIX_NEW = "new";

    public static final String METHODPREFIX_DESTROY = "destroy";

    public static final String SUFFIX = ".class";

    private static final String JAR_SUFFIX = ".jar!/";

    private static final String DELIMITER = "_";

    public S2Container build(String path) {
        return build(null, path);
    }

    S2Container build(S2Container parent, String path) {
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

        Dicon dicon = preparerClass.getAnnotation(Dicon.class);
        if (dicon != null && dicon.namespace().length() > 0) {
            container.setNamespace(dicon.namespace());
        }

        preparer.include();
        registerComponentDefs(container, preparer);

        String additionalDiconPath = constructAdditionalDiconPath(path);
        if (S2ContainerBuilderUtils.resourceExists(additionalDiconPath, this)) {
            S2ContainerBuilderUtils.mergeContainer(container,
                    S2ContainerFactory.create(additionalDiconPath));
        }

        return container;
    }

    protected ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    protected String constructAdditionalDiconPath(String path) {
        return constructRedifinitionDiconPath(path, null);
    }

    S2Container createContainer(S2Container parent, String path) {
        S2Container container = new S2ContainerImpl();
        container.setPath(path);
        if (parent != null) {
            container.setRoot(parent.getRoot());
        }
        return container;
    }

    void registerComponentDefs(S2Container container,
            S2ContainerPreparer preparer) {
        Method[] methods = preparer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            if (name.startsWith(METHODPREFIX_DEFINE)) {
                registerComponentDef(container, methods[i], preparer);
            }
        }
    }

    void registerComponentDef(S2Container container, Method method,
            S2ContainerPreparer preparer) {
        ComponentDef componentDef = constructComponentDef(method, preparer);

        if (componentDef.getComponentName() != null) {
            componentDef = redefine(componentDef, container.getPath());
        }

        container.register(componentDef);
    }

    ComponentDef redefine(ComponentDef componentDef, String path) {
        String name = componentDef.getComponentName();
        String diconPath = constructRedifinitionDiconPath(path, name);
        if (!S2ContainerBuilderUtils.resourceExists(diconPath, this)) {
            return componentDef;
        }

        S2Container container = S2ContainerFactory.create(diconPath);
        if (!container.hasComponentDef(name)) {
            throw new RuntimeException(
                    "Can't find component definition named '" + name + "' in "
                            + diconPath);
        }

        return container.getComponentDef(name);
    }

    protected String constructRedifinitionDiconPath(String path, String name) {
        String body;
        String suffix;
        int dot = path.lastIndexOf('.');
        if (dot < 0) {
            body = path;
            suffix = "";
        } else {
            body = path.substring(0, dot);
            suffix = path.substring(dot);
        }
        if (name == null) {
            name = "";
        }
        return body + DELIMITER + name + suffix;
    }

    ComponentDef constructComponentDef(Method method,
            S2ContainerPreparer preparer) {
        AnnotationHandler annoHandler = AnnotationHandlerFactory
                .getAnnotationHandler();

        String componentName = ClassBuilderUtils.toComponentName(method
                .getName().substring(METHODPREFIX_DEFINE.length()));
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new RuntimeException(
                    "Definition method must have only one parameter but "
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

        Component componentAnnotation = method.getAnnotation(Component.class);
        if (componentAnnotation != null) {
            componentDef.setInstanceDef(InstanceDefFactory
                    .getInstanceDef(componentAnnotation.instance().getName()));
            componentDef.setAutoBindingDef(AutoBindingDefFactory
                    .getAutoBindingDef(componentAnnotation.autoBinding()
                            .getName()));
        }

        Aspect aspectAnnotation = method.getAnnotation(Aspect.class);
        if (aspectAnnotation != null) {
            AspectDef aspectDef = AspectDefFactory.createAspectDef(
                    aspectAnnotation.value(), aspectAnnotation.pointcut());
            componentDef.addAspectDef(aspectDef);
        }

        annoHandler.appendInitMethod(componentDef);
        annoHandler.appendDestroyMethod(componentDef);

        InitMethodDef initMethodDef = new PreparerInitMethodDef(method);
        ArgDef argDef = new ArgDefImpl(preparer);
        initMethodDef.addArgDef(argDef);
        componentDef.addInitMethodDef(initMethodDef);

        Method destroyMethod = ClassBuilderUtils.findMethod(
                preparer.getClass(), componentName, METHODPREFIX_DESTROY);
        if (destroyMethod != null) {
            DestroyMethodDef destroyMethodDef = new PreparerDestroyMethodDef(
                    destroyMethod);
            argDef = new ArgDefImpl(preparer);
            destroyMethodDef.addArgDef(argDef);
            componentDef.addDestroyMethodDef(destroyMethodDef);
        }

        return componentDef;
    }

    @SuppressWarnings("unchecked")
    Class<? extends S2ContainerPreparer> getPreparerClass(String path) {
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
            return (Class<? extends S2ContainerPreparer>) clazz;
        } else {
            throw new RuntimeException("Not Preparer: " + path);
        }
    }

    Class getClassFromURL(String path, ClassLoader classLoader) {
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

    Class getClassFromClassName(String path, ClassLoader classLoader) {
        String className = path.substring(0, path.length() - SUFFIX.length())
                .replace('/', '.');
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    ClassLoader getClassLoaderForLoadingPreparer() {
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        // S2Container関連のクラスがコンテキストクラスローダから見えない場合に備えてこうしている。
        return new CompositeClassLoader(new ClassLoader[] { classLoader,
                getClass().getClassLoader() });
    }

    public S2Container include(S2Container parent, String path) {
        S2Container child = build(parent, path);
        parent.include(child);
        return child;
    }
}
