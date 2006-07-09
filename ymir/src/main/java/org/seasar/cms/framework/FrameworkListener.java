package org.seasar.cms.framework;

import javax.servlet.ServletContextEvent;

import org.seasar.cms.framework.container.hotdeploy.OndemandUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.servlet.S2ContainerListener;
import org.seasar.framework.log.Logger;

public class FrameworkListener extends S2ContainerListener {

    private static Logger logger = Logger.getLogger(FrameworkListener.class);

    public void contextInitialized(ServletContextEvent sce) {

        super.contextInitialized(sce);

        logger.debug("Framework initialize start");

        Configuration config = getConfiguration();
        if (config.getProperty(Configuration.KEY_WEBAPPROOT) == null) {
            config.setProperty(Configuration.KEY_WEBAPPROOT, sce
                .getServletContext().getRealPath("/"));
        }

        String projectStatus = config
            .getProperty(Configuration.KEY_PROJECTSTATUS);
        logger.info("Project status is: "
            + (projectStatus != null ? projectStatus : "(UNDEFINED)"));

        // developモード以外の時はhotdeployを無効にするために
        // こうしている。
        if (!Configuration.PROJECTSTATUS_DEVELOP.equals(projectStatus)) {
            OndemandUtils.start(getContainer(), true);
        }

        logger.debug("Framework initialize end");
    }

    public void contextDestroyed(ServletContextEvent sce) {

        logger.debug("Framework destroy start");

        if (!Configuration.PROJECTSTATUS_DEVELOP.equals(getConfiguration()
            .getProperty(Configuration.KEY_PROJECTSTATUS))) {

            OndemandUtils.stop(getContainer(), true);
        }

        super.contextDestroyed(sce);

        logger.debug("Framework destroy end");
    }

    S2Container getContainer() {

        return SingletonS2ContainerFactory.getContainer();
    }

    Configuration getConfiguration() {

        return (Configuration) getContainer().getComponent(Configuration.class);
    }
}
