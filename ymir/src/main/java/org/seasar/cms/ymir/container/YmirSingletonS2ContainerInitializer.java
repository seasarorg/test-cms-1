package org.seasar.cms.ymir.container;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
import org.seasar.framework.container.factory.CircularIncludeRuntimeException;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.impl.servlet.HttpServletExternalContext;
import org.seasar.framework.container.impl.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.util.StringUtil;

public class YmirSingletonS2ContainerInitializer {

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

        include(container, ContainerUtils
            .getResourceURLs(Globals.COMPONENTS_DICON));
    }

    void include(S2Container parent, URL[] pathURLs) {

        include(parent, new HashSet(Arrays.asList(pathURLs)));
    }

    Set include(S2Container parent, Set pathURLSet) {

        Set remain = pathURLSet;

        int size = parent.getChildSize();
        if (size == 0) {
            remain = doInclude(parent, remain);
        } else {
            for (int i = 0; i < size; i++) {
                remain = include(parent.getChild(i), remain);
            }
            if (!remain.isEmpty()) {
                remain = doInclude(parent, remain);
            }
        }
        return remain;
    }

    private Set doInclude(S2Container container, Set pathURLSet) {

        Set remain = new HashSet(pathURLSet);
        for (Iterator itr = pathURLSet.iterator(); itr.hasNext();) {
            URL pathURL = (URL) itr.next();
            String path = pathURL.toExternalForm();
            try {
                // FIXME S2ContainerFactory.include()で適切に循環参照のチェックを
                // してくれるようになったらこのロジックは不要。
                traverse(S2ContainerFactory.create(path), container.getPath());

                S2ContainerFactory.include(container, path);
                remain.remove(pathURL);
            } catch (CircularIncludeRuntimeException ignore) {
            }
        }
        return remain;
    }

    private void traverse(S2Container container, String path) {
        int size = container.getChildSize();
        for (int i = 0; i < size; i++) {
            S2Container child = container.getChild(i);
            if (path.equals(child.getPath())) {
                throw new CircularIncludeRuntimeException(path, new HashSet());
            } else {
                traverse(child, path);
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
