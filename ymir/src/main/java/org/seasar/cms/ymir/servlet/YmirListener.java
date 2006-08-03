package org.seasar.cms.ymir.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.seasar.cms.ymir.YmirBootstrap;

public class YmirListener implements ServletContextListener {

    public static final String YMIR_KEY = "org.seasar.cms.ymir.ymir";

    private YmirBootstrap bootstrap_ = new YmirBootstrap();

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        try {
            sc.setAttribute(YMIR_KEY, bootstrap_.init(sc));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        sc.removeAttribute(YMIR_KEY);
        bootstrap_.destroy();
    }
}
