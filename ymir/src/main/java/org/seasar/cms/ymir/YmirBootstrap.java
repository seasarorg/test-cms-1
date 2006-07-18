package org.seasar.cms.ymir;

import javax.servlet.ServletContext;

import org.seasar.cms.ymir.container.YmirSingletonS2ContainerInitializer;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class YmirBootstrap {

    private Ymir ymir_;

    public Ymir init(ServletContext servletContext, String configPath) {

        initializeContainer(servletContext, configPath);
        ymir_ = (Ymir) getContainer().getComponent(Ymir.class);
        initializeConfiguration(ymir_, servletContext);
        ymir_.init();
        return ymir_;
    }

    void initializeContainer(ServletContext servletContext, String configPath) {

        YmirSingletonS2ContainerInitializer initializer = new YmirSingletonS2ContainerInitializer();
        initializer.setConfigPath(configPath);
        initializer.setApplication(servletContext);
        initializer.initialize();
    }

    void initializeConfiguration(Ymir ymir, ServletContext servletContext) {

        Configuration config = ymir.getConfiguration();
        if (config.getProperty(Configuration.KEY_WEBAPPROOT) == null) {
            config.setProperty(Configuration.KEY_WEBAPPROOT, servletContext
                .getRealPath("/"));
        }
    }

    public void destroy() {

        if (ymir_ != null) {
            ymir_.destroy();
        }
        SingletonS2ContainerFactory.destroy();
    }

    S2Container getContainer() {

        return SingletonS2ContainerFactory.getContainer();
    }
}
