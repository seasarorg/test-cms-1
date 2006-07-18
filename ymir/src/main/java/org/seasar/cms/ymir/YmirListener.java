package org.seasar.cms.ymir;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class YmirListener implements ServletContextListener {

    public static final String CONFIG_PATH_KEY = "org.seasar.framework.container.configPath";

    public static final String YMIR_KEY = "org.seasar.cms.ymir.ymir";

    private YmirBootstrap bootstrap_ = new YmirBootstrap();

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        sc.setAttribute(YMIR_KEY, bootstrap_.init(sc, sc
            .getInitParameter(CONFIG_PATH_KEY)));
    }

    public void contextDestroyed(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        sc.removeAttribute(YMIR_KEY);
        bootstrap_.destroy();
    }
}
