package org.seasar.cms.pluggable.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.S2Container;

public class PluggableListener implements ServletContextListener {

    public static final String CONFIG_PATH_KEY = "org.seasar.framework.container.configPath";

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        String configPath = sc.getInitParameter(CONFIG_PATH_KEY);

        try {
            SingletonPluggableContainerFactory.setApplication(sc);
            SingletonPluggableContainerFactory.prepareForContainer();

            if (configPath != null) {
                SingletonPluggableContainerFactory.integrate(configPath,
                        new S2Container[0]);
            }

            SingletonPluggableContainerFactory.init();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        SingletonPluggableContainerFactory.destroy();
    }
}
