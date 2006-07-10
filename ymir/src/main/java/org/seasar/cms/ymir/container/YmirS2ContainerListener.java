package org.seasar.cms.ymir.container;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.log.Logger;

public class YmirS2ContainerListener implements ServletContextListener {

    public static final String CONFIG_PATH_KEY = "org.seasar.framework.container.configPath";

    protected Logger logger_ = Logger.getLogger(getClass());

    private void initializeContainer(ServletContext servletContext) {
        String configPath = servletContext.getInitParameter(CONFIG_PATH_KEY);
        YmirSingletonS2ContainerInitializer initializer = new YmirSingletonS2ContainerInitializer();
        initializer.setConfigPath(configPath);
        initializer.setApplication(servletContext);
        initializer.initialize();
    }

    public void contextInitialized(ServletContextEvent event) {
        logger_.debug("S2Container initialize start");
        ServletContext servletContext = event.getServletContext();
        try {
            initializeContainer(servletContext);
        } catch (RuntimeException e) {
            logger_.log(e);
            throw e;
        }
        logger_.debug("S2Container initialize end");
    }

    public void contextDestroyed(ServletContextEvent event) {
        SingletonS2ContainerFactory.destroy();
    }

}
