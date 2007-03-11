package org.seasar.cms.ymir.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.seasar.cms.ymir.YmirBootstrap;

public class YmirListener implements ServletContextListener {

    public static final String ATTR_YMIR = "org.seasar.cms.ymir.ymir";

    private YmirBootstrap bootstrap_ = new YmirBootstrap();

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        try {
            sc.setAttribute(ATTR_YMIR, bootstrap_.init());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        sc.removeAttribute(ATTR_YMIR);
        bootstrap_.destroy();
    }
}
