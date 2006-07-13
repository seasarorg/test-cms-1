package org.seasar.cms.ymir.impl;

import java.util.Arrays;

import javax.servlet.ServletContext;

import org.seasar.cms.ymir.Configuration;
import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.cms.ymir.Ymir;
import org.seasar.cms.ymir.container.YmirSingletonS2ContainerInitializer;
import org.seasar.cms.ymir.container.hotdeploy.OndemandUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.log.Logger;

public class YmirImpl implements Ymir {

    private LifecycleListener[] lifecycleListeners_;

    private Logger logger_ = Logger.getLogger(getClass());

    public void init(ServletContext servletContext, String configPath) {

        logger_.debug("Ymir initialize start");

        initializeContainer(servletContext, configPath);
        initializeConfiguration(servletContext);
        initializeInternalComponents();
        initializeListeners();

        logger_.debug("Ymir initialize end");
    }

    void initializeListeners() {

        for (int i = 0; i < lifecycleListeners_.length; i++) {
            lifecycleListeners_[i].init();
        }
    }

    void initializeContainer(ServletContext servletContext, String configPath) {

        YmirSingletonS2ContainerInitializer initializer = new YmirSingletonS2ContainerInitializer();
        initializer.setConfigPath(configPath);
        initializer.setApplication(servletContext);
        initializer.initialize();

        String projectStatus = getConfiguration().getProperty(
            Configuration.KEY_PROJECTSTATUS);
        logger_.info("Project status is: "
            + (projectStatus != null ? projectStatus : "(UNDEFINED)"));

        // developモード以外の時はhotdeployを無効にするために
        // こうしている。
        if (!Configuration.PROJECTSTATUS_DEVELOP.equals(projectStatus)) {
            OndemandUtils.start(getContainer(), true);
        }
    }

    void initializeConfiguration(ServletContext servletContext) {

        Configuration config = getConfiguration();
        if (config.getProperty(Configuration.KEY_WEBAPPROOT) == null) {
            config.setProperty(Configuration.KEY_WEBAPPROOT, servletContext
                .getRealPath("/"));
        }
    }

    void initializeInternalComponents() {

        // FIXME findAllComponents()を使うようにしよう。
        lifecycleListeners_ = (LifecycleListener[]) Arrays.asList(
            getContainer().findComponents(LifecycleListener.class)).toArray(
            new LifecycleListener[0]);
    }

    public void destroy() {

        logger_.debug("Ymir destroy start");

        destroyListeners();
        destroyContainer();

        logger_.debug("Ymir destroy end");
    }

    void destroyListeners() {

        for (int i = 0; i < lifecycleListeners_.length; i++) {
            lifecycleListeners_[i].destroy();
        }
    }

    void destroyContainer() {

        if (!Configuration.PROJECTSTATUS_DEVELOP.equals(getConfiguration()
            .getProperty(Configuration.KEY_PROJECTSTATUS))) {

            OndemandUtils.stop(getContainer(), true);
        }

        SingletonS2ContainerFactory.destroy();
    }

    S2Container getContainer() {

        return SingletonS2ContainerFactory.getContainer();
    }

    Configuration getConfiguration() {

        return (Configuration) getContainer().getComponent(Configuration.class);
    }
}
