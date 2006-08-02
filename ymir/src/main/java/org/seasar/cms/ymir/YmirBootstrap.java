package org.seasar.cms.ymir;

import javax.servlet.ServletContext;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class YmirBootstrap {

    private Ymir ymir_;

    public Ymir init(ServletContext servletContext) {

        ymir_ = (Ymir) getContainer().getComponent(Ymir.class);
        initializeConfiguration(ymir_, servletContext);
        ymir_.init();
        return ymir_;
    }

    void initializeConfiguration(Ymir ymir, ServletContext servletContext) {

        Configuration config = getConfiguration();
        if (config.getProperty(Globals.KEY_WEBAPPROOT) == null) {
            config.setProperty(Globals.KEY_WEBAPPROOT, servletContext
                .getRealPath("/"));
        }
    }

    Configuration getConfiguration() {

        return (Configuration) getContainer().getComponent(Configuration.class);
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
