package org.seasar.cms.pluggable.impl;

import static org.seasar.cms.pluggable.Configuration.KEY_PROJECTSTATUS;
import static org.seasar.cms.pluggable.Configuration.KEY_S2CONTAINER_DISABLE_HOTDEPLOY;
import static org.seasar.cms.pluggable.Configuration.PROJECTSTATUS_DEVELOP;

import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.PluggableContainerFactory;
import org.seasar.cms.pluggable.PluggableProvider;
import org.seasar.cms.pluggable.hotdeploy.DistributedHotdeployBehavior;
import org.seasar.cms.pluggable.util.PluggableUtils;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.ExternalContextComponentDefRegister;
import org.seasar.framework.container.MetaDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.autoregister.AbstractAutoRegister;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
import org.seasar.framework.container.external.servlet.HttpServletExternalContext;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.container.util.Traversal;
import org.seasar.framework.container.util.Traversal.ComponentDefHandler;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.DisposableUtil;

public class PluggableContainerFactoryImpl implements PluggableContainerFactory {

    private String configPath_ = ROOT_DICON;

    private ExternalContext externalContext_;

    private ExternalContextComponentDefRegister externalContextComponentDefRegister_;

    public Object application_;

    private S2Container rootContainer_;

    private Logger logger_ = Logger.getLogger(getClass());

    private boolean initialized_;

    public PluggableContainerFactoryImpl() {
    }

    public String getConfigPath() {
        return configPath_;
    }

    public void setConfigPath(String path) {
        configPath_ = path;
    }

    public ExternalContext getExternalContext() {
        return externalContext_;
    }

    public void setExternalContext(ExternalContext extCtx) {
        externalContext_ = extCtx;
    }

    public ExternalContextComponentDefRegister getExternalContextComponentDefRegister() {
        return externalContextComponentDefRegister_;
    }

    public void setExternalContextComponentDefRegister(
            ExternalContextComponentDefRegister extCtxComponentDefRegister) {
        externalContextComponentDefRegister_ = extCtxComponentDefRegister;
    }

    public Object getApplication() {
        return application_;
    }

    public void setApplication(Object application) {
        application_ = application;
    }

    public void prepareForContainer() {
        if (rootContainer_ != null) {
            return;
        }
        initializeComponentDeployerProvider();
        rootContainer_ = createS2Container(configPath_);
        prepareForExternalContext();
    }

    S2Container createS2Container(String path) {
        try {
            return S2ContainerFactory.create(path);
        } catch (RuntimeException ex) {
            throw new RuntimeException(
                    "Can't create S2Container: path=" + path, ex);
        }
    }

    S2Container includeS2Container(S2Container parent, String path) {
        try {
            return S2ContainerFactory.include(parent, path);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Can't include: parent="
                    + parent.getPath() + ", path=" + path, ex);
        }
    }

    void prepareForExternalContext() {
        if (rootContainer_ == null) {
            throw new IllegalStateException("rootContainer is null");
        }
        if (initialized_) {
            throw new IllegalStateException("Already initialized");
        }

        if (rootContainer_.getExternalContext() == null) {
            if (externalContext_ != null) {
                rootContainer_.setExternalContext(externalContext_);
            }
        }
        if (rootContainer_.getExternalContext().getApplication() == null) {
            if (application_ != null) {
                rootContainer_.getExternalContext()
                        .setApplication(application_);
            }
        }
        if (rootContainer_.getExternalContextComponentDefRegister() == null
                && externalContextComponentDefRegister_ != null) {
            rootContainer_
                    .setExternalContextComponentDefRegister(externalContextComponentDefRegister_);
        }
    }

    void initializeComponentDeployerProvider() {
        if (ComponentDeployerFactory.getProvider() instanceof ComponentDeployerFactory.DefaultProvider) {
            ComponentDeployerFactory
                    .setProvider(new ExternalComponentDeployerProvider());
        }
    }

    public S2Container integrate(String configPath, S2Container[] dependencies) {

        return integrate(configPath, null, dependencies);
    }

    public S2Container integrate(String configPath, ClassLoader classLoader,
            S2Container[] dependencies) {

        S2Container instance;
        final ClassLoader oldLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(classLoader);
            }

            if (configPath == null) {
                instance = PluggableUtils.newContainer();
                rootContainer_.include(instance);
            } else {
                instance = includeS2Container(rootContainer_, configPath);
            }
            integrate(rootContainer_, instance, dependencies);
            invokeAutoRegisters(instance);
            return instance;
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    void invokeAutoRegisters(S2Container container) {
        Traversal.forEachComponent(container, new ComponentDefHandler() {
            public Object processComponent(ComponentDef componentDef) {
                Class<?> componentClass = componentDef.getComponentClass();
                if (componentClass != null
                        && AbstractAutoRegister.class
                                .isAssignableFrom(componentClass)) {
                    // *.diconをロードする際にS2Containerが自動的に
                    // <initMethod="registerAll" />をつけてくれるため、
                    // コンポーネントのインスタンス化の際に自動的にregisterAll()が呼ばれる。
                    componentDef.getComponent();
                }
                return null;
            }
        }, false);
    }

    void integrate(S2Container root, S2Container container,
            S2Container[] dependencies) {

        integrate(root, container, dependencies, PluggableUtils
                .getResourceURLs(COMPONENTS_DICON));
    }

    void integrate(S2Container root, S2Container container,
            S2Container[] dependencies, URL[] pathURLs) {

        for (int i = 0; i < pathURLs.length; i++) {
            addAll(container, readS2Container(pathURLs[i].toExternalForm()));
        }

        includeToLeaves(container, dependencies);
    }

    public S2Container processExpanding(S2Container container) {
        MetaDef metaDef = container.getMetaDef(META_EXPAND);
        if (metaDef == null) {
            return container;
        }

        Object value = metaDef.getValue();
        if (value == null) {
            throw new NullPointerException("container '" + container.getPath()
                    + "' has null 'expand' meta-data");
        }

        String[] paths = value.toString().split(",");
        for (int i = 0; i < paths.length; i++) {
            URL[] urls = PluggableUtils.getResourceURLs(paths[i].trim());
            if (urls.length == 1) {
                addAll(container, processExpanding(readS2Container(urls[0]
                        .toExternalForm())));
            } else if (urls.length == 0) {
                throw new RuntimeException(
                        "Resource to expand not found: container="
                                + container.getPath() + ", name=" + paths[i]);
            } else {
                StringBuffer sb = new StringBuffer();
                String delim = "";
                for (int j = 0; j < urls.length; j++) {
                    sb.append(delim).append(urls[j].toExternalForm());
                    delim = ", ";
                }
                throw new RuntimeException(
                        "Too many resources to expand: container="
                                + container.getPath() + ", name=" + paths[i]
                                + ", resources=" + sb.toString());
            }
        }

        return container;
    }

    S2Container readS2Container(String path) {
        PluggableProvider.setUsingPluggableRoot(true);
        try {
            return createS2Container(path);
        } finally {
            PluggableProvider.setUsingPluggableRoot(false);
        }
    }

    void addAll(S2Container container, S2Container added) {
        includeChildren(container, added);
        int size = added.getMetaDefSize();
        for (int j = 0; j < size; j++) {
            MetaDef md = added.getMetaDef(j);
            container.addMetaDef(md);
        }
        size = added.getComponentDefSize();
        for (int j = 0; j < size; j++) {
            ComponentDef cd = added.getComponentDef(j);
            cd.setContainer(null);
            container.register(cd);
        }
    }

    void includeChildren(S2Container container, S2Container target) {
        Set<S2Container> childSet = new HashSet<S2Container>();
        int size = container.getChildSize();
        for (int i = 0; i < size; i++) {
            childSet.add(container.getChild(i));
        }

        size = target.getChildSize();
        for (int i = 0; i < size; i++) {
            S2Container child = target.getChild(i);
            if (!childSet.contains(child)) {
                container.include(child);
                childSet.add(child);
            }
        }
    }

    void includeToLeaves(S2Container container, S2Container[] dependencies) {
        int size = container.getChildSize();
        if (size == 0) {
            include(container, dependencies);
        } else {
            for (int i = 0; i < size; i++) {
                includeToLeaves(container.getChild(i), dependencies);
            }
        }
    }

    void include(S2Container container, S2Container[] dependencies) {
        if (isGlobalDicon(container.getPath())) {
            return;
        }
        if (dependencies.length > 0) {
            for (int i = 0; i < dependencies.length; i++) {
                include(container, dependencies[i]);
            }
        } else {
            includeS2Container(container, GLOBAL_DICON);
        }
    }

    boolean isGlobalDicon(String path) {
        if (GLOBAL_DICON.equals(path)) {
            return true;
        } else {
            URL global = Thread.currentThread().getContextClassLoader()
                    .getResource(GLOBAL_DICON);
            return (global != null && global.toExternalForm().equals(path));
        }
    }

    void include(S2Container parent, S2Container child) {
        if (!isCircular(child, parent, new LinkedList<String>())) {
            parent.include(child);
        }
    }

    boolean isCircular(final S2Container container, final S2Container target,
            LinkedList<String> paths) {
        paths.addFirst(container.getPath());
        try {
            if (container == target) {
                return true;
            }
            for (int i = 0; i < container.getChildSize(); ++i) {
                if (isCircular(container.getChild(i), target, paths)) {
                    return true;
                }
            }
            return false;
        } finally {
            paths.removeFirst();
        }
    }

    public void init() {
        if (rootContainer_ == null) {
            throw new IllegalStateException("Container is not prepared yet");
        }

        if (initialized_) {
            return;
        }
        initialized_ = true;

        if (application_ != null) {
            if (rootContainer_.getExternalContext() == null) {
                HttpServletExternalContext extCtx = new HttpServletExternalContext();
                extCtx.setApplication(application_);
                rootContainer_.setExternalContext(extCtx);
            } else if (rootContainer_.getExternalContext().getApplication() == null) {
                rootContainer_.getExternalContext()
                        .setApplication(application_);
            }
        }

        rootContainer_.init();

        Configuration configuration = getConfiguration();
        String projectStatus = configuration.getProperty(KEY_PROJECTSTATUS);
        logger_.info("Project status is: "
                + (projectStatus != null ? projectStatus : "(UNDEFINED)"));

        boolean hotdeployEnabled = PROJECTSTATUS_DEVELOP.equals(projectStatus)
                && !"true".equals(configuration
                        .getProperty(KEY_S2CONTAINER_DISABLE_HOTDEPLOY));
        if (hotdeployEnabled) {
            logger_.info("HOT Deploy is enabled");
        } else {
            logger_.info("HOT Deploy is disabled");
        }
        getHotdeployBehavior().init(hotdeployEnabled);
    }

    DistributedHotdeployBehavior getHotdeployBehavior() {
        return ((DistributedHotdeployBehavior) S2ContainerBehavior
                .getProvider());
    }

    Configuration getConfiguration() {
        return (Configuration) rootContainer_.getComponent(Configuration.class);
    }

    public void destroy() {
        initialized_ = false;
        if (rootContainer_ == null) {
            return;
        }

        getHotdeployBehavior().destroy();

        rootContainer_.destroy();
        rootContainer_ = null;
        DisposableUtil.dispose();
    }

    public S2Container getRootContainer() {
        if (rootContainer_ == null) {
            throw new EmptyRuntimeException("S2Container");
        }
        return rootContainer_;
    }

    public void setRootContainer(S2Container rootContainer) {
        rootContainer_ = rootContainer;
    }

    public boolean hasRootContainer() {
        return rootContainer_ != null;
    }
}
