package org.seasar.cms.pluggable.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.PluggableContainerFactory;
import org.seasar.cms.pluggable.hotdeploy.DistributedOndemandBehavior;
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
import org.seasar.framework.container.factory.CircularIncludeRuntimeException;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.container.util.Traversal;
import org.seasar.framework.container.util.Traversal.ComponentDefHandler;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.DisposableUtil;

public class PluggableContainerFactoryImpl implements PluggableContainerFactory {

    private String configPath = ROOT_DICON;

    private ExternalContext externalContext_;

    private ExternalContextComponentDefRegister externalContextComponentDefRegister_;

    public Object application_;

    private S2Container rootContainer_;

    private Logger logger_ = Logger.getLogger(getClass());

    private boolean initialized_;

    public PluggableContainerFactoryImpl() {
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String path) {
        configPath = path;
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
        rootContainer_ = createS2Container(configPath);
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
        } else if (rootContainer_.getExternalContext().getApplication() == null
                && externalContext_ != null) {
            rootContainer_.getExternalContext().setApplication(
                    externalContext_.getApplication());
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
                if (AbstractAutoRegister.class.isAssignableFrom(componentDef
                        .getComponentClass())) {
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

        includeToLeaves(container, dependencies);

        for (int i = 0; i < pathURLs.length; i++) {
            S2Container c = createS2Container(pathURLs[i].toExternalForm());
            includeChildren(container, c);
            int size = c.getMetaDefSize();
            for (int j = 0; j < size; j++) {
                MetaDef md = c.getMetaDef(j);
                container.addMetaDef(md);
            }
            size = c.getComponentDefSize();
            for (int j = 0; j < size; j++) {
                ComponentDef cd = c.getComponentDef(j);
                cd.setContainer(null);
                container.register(cd);
            }
        }
    }

    void includeChildren(S2Container container, S2Container parent) {
        Set childSet = new HashSet();
        int size = container.getChildSize();
        for (int i = 0; i < size; i++) {
            childSet.add(container.getChild(i));
        }

        size = parent.getChildSize();
        for (int i = 0; i < size; i++) {
            S2Container child = parent.getChild(i);
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
        assertNotCircularInclude(child, parent, new LinkedList());
        parent.include(child);
    }

    void assertNotCircularInclude(final S2Container container,
            final S2Container target, LinkedList paths) {
        paths.addFirst(container.getPath());
        try {
            if (container == target) {
                throw new CircularIncludeRuntimeException(target.getPath(),
                        new ArrayList(paths));
            }
            for (int i = 0; i < container.getChildSize(); ++i) {
                assertNotCircularInclude(container.getChild(i), target, paths);
            }
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

        String projectStatus = getConfiguration().getProperty(
                Configuration.KEY_PROJECTSTATUS);
        logger_.info("Project status is: "
                + (projectStatus != null ? projectStatus : "(UNDEFINED)"));

        // developモード以外の時はhotdeployを無効にするために
        // こうしている。
        if (!Configuration.PROJECTSTATUS_DEVELOP.equals(projectStatus)) {
            getOndemandBehavior().start();
        }
    }

    DistributedOndemandBehavior getOndemandBehavior() {
        return ((DistributedOndemandBehavior) S2ContainerBehavior.getProvider());
    }

    Configuration getConfiguration() {
        return (Configuration) rootContainer_.getComponent(Configuration.class);
    }

    public void destroy() {
        initialized_ = false;
        if (rootContainer_ == null) {
            return;
        }

        if (!getConfiguration().equalsProjectStatus(
                Configuration.PROJECTSTATUS_DEVELOP)) {

            getOndemandBehavior().stop();
        }

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
