package org.seasar.cms.pluggable;

import org.seasar.cms.pluggable.impl.PluggableContainerFactoryImpl;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class SingletonPluggableContainerFactory {

    private static PluggableContainerFactory instance_ = new PluggableContainerFactoryImpl();

    private SingletonPluggableContainerFactory() {
    }

    public static PluggableContainerFactory getInstance() {
        return instance_;
    }

    public static void setInstance(PluggableContainerFactory instance) {
        instance_ = instance;
    }

    public static String getConfigPath() {
        return instance_.getConfigPath();
    }

    public static void setConfigPath(String path) {
        instance_.setConfigPath(path);
    }

    public static Object getApplication() {
        return instance_.getApplication();
    }

    public static void setApplication(Object application) {
        instance_.setApplication(application);
    }

    public static void prepareForContainer() {
        instance_.prepareForContainer();
        SingletonS2ContainerFactory.setContainer(instance_.getRootContainer());
    }

    public static S2Container integrate(String configPath,
            S2Container[] dependencies) {
        return instance_.integrate(configPath, dependencies);
    }

    public static S2Container integrate(String configPath,
            ClassLoader classLoader, S2Container[] dependencies) {
        return instance_.integrate(configPath, classLoader, dependencies);
    }

    public static void init() {
        instance_.init();
    }

    public static void destroy() {
        instance_.destroy();
        SingletonS2ContainerFactory.setContainer(null);
    }

    public static S2Container getRootContainer() {
        return instance_.getRootContainer();
    }

    public static void setRootContainer(S2Container container) {
        instance_.setRootContainer(container);
    }

    public static boolean hasRootContainer() {
        return instance_.hasRootContainer();
    }
}
