package org.seasar.cms.ymir.container;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.impl.servlet.HttpServletExternalContext;
import org.seasar.framework.container.impl.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.StringUtil;

class YmirSingletonS2ContainerInitializer {

    private Object application;

    private String configPath;

    private Logger logger_ = Logger.getLogger(getClass());

    public void initialize() {
        if (isAlreadyInitialized()) {
            return;
        }
        if (!StringUtil.isEmpty(configPath)) {
            SingletonS2ContainerFactory.setConfigPath(configPath);
        }
        if (ComponentDeployerFactory.getProvider() instanceof ComponentDeployerFactory.DefaultProvider) {
            ComponentDeployerFactory
                .setProvider(new ExternalComponentDeployerProvider());
        }

        S2Container container = S2ContainerFactory
            .create(SingletonS2ContainerFactory.getConfigPath());

        integrate(container);

        if (container.getExternalContext() == null) {
            HttpServletExternalContext extCtx = new HttpServletExternalContext();
            extCtx.setApplication(application);
            container.setExternalContext(extCtx);
        } else if (container.getExternalContext().getApplication() == null) {
            container.getExternalContext().setApplication(application);
        }
        if (container.getExternalContextComponentDefRegister() == null) {
            container
                .setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
        }
        container.init();
        SingletonS2ContainerFactory.setContainer(container);
    }

    void integrate(S2Container container) {
        includeContainer(container, gatherContainers());
    }

    public S2Container[] gatherContainers() {

        URL[] urls = getResourceURLs(Globals.COMPONENTS_DICON);
        List containerList = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            try {
                containerList.add(createContainer(urls[i]));
            } catch (RuntimeException ignored) {
                if (logger_.isInfoEnabled()) {
                    logger_.info("Can't read configuration: " + urls[i]);
                }
            }
        }
        return (S2Container[]) containerList.toArray(new S2Container[0]);
    }

    URL[] getResourceURLs(String path) {
        return ContainerUtils.getResourceURLs(path);
    }

    S2Container createContainer(URL url) {

        return S2ContainerFactory.create(url.toExternalForm());
    }

    void includeContainer(S2Container parent, S2Container[] children) {

        int size = parent.getChildSize();
        if (size == 0) {
            for (int i = 0; i < children.length; i++) {
                parent.include(children[i]);
            }
        } else {
            for (int i = 0; i < size; i++) {
                includeContainer(parent.getChild(i), children);
            }
        }
    }

    private boolean isAlreadyInitialized() {
        return SingletonS2ContainerFactory.hasContainer();
    }

    public void setApplication(Object application) {
        this.application = application;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
