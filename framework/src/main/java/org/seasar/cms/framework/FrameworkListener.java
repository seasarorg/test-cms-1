package org.seasar.cms.framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class FrameworkListener implements ServletContextListener {

    public static final String NAME_CONFIG = "config";

    public static final String CONFIG_PATH_KEY = "org.seasar.cms.framework.configPath";

    private static final String PROP_ROOTPATH = "webapp.rootPath";

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext servletContext = sce.getServletContext();
        String configPath = servletContext.getInitParameter(CONFIG_PATH_KEY);

        Properties config = new Properties();

        config.setProperty(PROP_ROOTPATH, servletContext.getRealPath("/"));

        InputStream inputStream = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream(configPath);
        if (inputStream == null) {
            throw new RuntimeException("Configuration '" + configPath
                + "' does not exist in contextClassLoader");
        }
        try {
            config.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Can't read configuration '"
                + configPath + "'", ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                ;
            }
        }
        SingletonS2ContainerFactory.getContainer()
            .register(config, NAME_CONFIG);
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
