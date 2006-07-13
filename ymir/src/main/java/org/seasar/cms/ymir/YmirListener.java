package org.seasar.cms.ymir;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.seasar.cms.ymir.impl.YmirImpl;

public class YmirListener implements ServletContextListener {

    public static final String CONFIG_PATH_KEY = "org.seasar.framework.container.configPath";

    private Ymir ymir_ = new YmirImpl();

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        ymir_.init(sc, sc.getInitParameter(CONFIG_PATH_KEY));
    }

    public void contextDestroyed(ServletContextEvent sce) {

        ymir_.destroy();
    }
}
