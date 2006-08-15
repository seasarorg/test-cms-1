package org.seasar.cms.ymir;

import javax.servlet.ServletContext;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.ymir.impl.SingleApplication;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class YmirBootstrap {

    private Ymir ymir_;

    public Ymir init() {

        ymir_ = (Ymir) getContainer().getComponent(Ymir.class);
        initializeApplication(ymir_, getServletContext());
        ymir_.init();
        return ymir_;
    }

    ServletContext getServletContext() {
        return (ServletContext) getContainer().getComponent(
                ServletContext.class);
    }

    void initializeApplication(Ymir ymir, ServletContext servletContext) {

        Class landmark = null;
        try {
            landmark = Class.forName(Globals.LANDMARK_CLASSNAME);
        } catch (ClassNotFoundException ignored) {
        }
        Application application = new SingleApplication(getConfiguration(),
                servletContext.getRealPath("/"), landmark);
        servletContext.setAttribute(Globals.ATTR_APPLICATION, application);

        ApplicationManager applicationManager = (ApplicationManager) getContainer()
                .getComponent(ApplicationManager.class);
        applicationManager.addApplication(application);
    }

    Configuration getConfiguration() {

        return (Configuration) getContainer().getComponent(Configuration.class);
    }

    public void destroy() {

        if (ymir_ != null) {
            ymir_.destroy();
        }
    }

    S2Container getContainer() {

        return SingletonS2ContainerFactory.getContainer();
    }
}
