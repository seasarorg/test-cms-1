package org.seasar.cms.ymir.container;

import java.net.URL;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.impl.servlet.HttpServletExternalContext;
import org.seasar.framework.container.impl.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.util.StringUtil;

public class YmirSingletonS2ContainerInitializer {

    private static final String ROOT_DICON = "root.dicon";

    private Object application;

    private String configPath;

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

        S2Container rootContainer = S2ContainerFactory.create(ROOT_DICON);
        S2Container container = S2ContainerFactory.include(rootContainer,
            SingletonS2ContainerFactory.getConfigPath());

        integrate(rootContainer, container);

        if (rootContainer.getExternalContext() == null) {
            HttpServletExternalContext extCtx = new HttpServletExternalContext();
            extCtx.setApplication(application);
            rootContainer.setExternalContext(extCtx);
        } else if (rootContainer.getExternalContext().getApplication() == null) {
            rootContainer.getExternalContext().setApplication(application);
        }
        if (rootContainer.getExternalContextComponentDefRegister() == null) {
            rootContainer
                .setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
        }
        rootContainer.init();
        SingletonS2ContainerFactory.setContainer(rootContainer);
    }

    void integrate(S2Container root, S2Container child) {
        integrate(root, child, ContainerUtils
            .getResourceURLs(Globals.COMPONENTS_DICON));
    }

    void integrate(S2Container root, S2Container child, URL[] pathURLs) {
        for (int i = 0; i < pathURLs.length; i++) {
            S2Container container = S2ContainerFactory.include(root,
                pathURLs[i].toExternalForm());
            container.include(child);
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
