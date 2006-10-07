package org.seasar.cms.pluggable.hotdeploy;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.HotdeployListener;
import org.seasar.framework.container.hotdeploy.OndemandProject;
import org.seasar.framework.container.impl.S2ContainerBehavior.DefaultProvider;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.DisposableUtil;

public class DistributedOndemandBehavior extends DefaultProvider {

    private OndemandProject[] projects_;

    private boolean hotdeployEnabled_;

    private LocalOndemandS2Container[] ondemandContainers_;

    private int counter_ = 0;

    private Logger logger_ = Logger.getLogger(getClass());

    public void init(boolean hotdeployEnabled) {
        hotdeployEnabled_ = hotdeployEnabled;
        initializeLocalOndemandS2Containers();
    }

    void initializeLocalOndemandS2Containers() {
        S2Container container = getContainer();

        // LocalOndemandS2Containerを集める。
        ComponentDef[] componentDefs = container
                .findAllComponentDefs(LocalOndemandS2Container.class);
        ondemandContainers_ = new LocalOndemandS2Container[componentDefs.length];
        for (int i = 0; i < componentDefs.length; i++) {
            ComponentDef cd = componentDefs[i];
            LocalOndemandS2Container ondemandContainer = (LocalOndemandS2Container) cd
                    .getComponent();

            // このLocalOndemandS2Containerが登録されているコンテナから見える
            // HotdeployListenerを登録する。
            HotdeployListener[] listeners = (HotdeployListener[]) cd
                    .getContainer().findAllComponents(HotdeployListener.class);
            for (int j = 0; j < listeners.length; j++) {
                HotdeployListener listener = listeners[j];
                if (listener instanceof LocalOndemandS2Container
                        && listener != ondemandContainer) {
                    // このLocalOndemandS2Containerが登録されているコンテナから見えても、
                    // 自分以外のLocalOndemandS2Containerは登録しない。
                    continue;
                }
                ondemandContainer.addHotdeployListener(listener);
            }
            ondemandContainer.init(hotdeployEnabled_);
            ondemandContainers_[i] = ondemandContainer;
        }
    }

    public void destroy() {
        for (int i = 0; i < ondemandContainers_.length; i++) {
            ondemandContainers_[i].destroy();
        }
        hotdeployEnabled_ = false;
        projects_ = null;
    }

    public synchronized void start() {
        if (logger_.isDebugEnabled()) {
            logger_.debug("OndemandBehavior's start() method called");
        }
        if (!hotdeployEnabled_) {
            return;
        }

        if (counter_++ == 0) {
            if (logger_.isDebugEnabled()) {
                logger_.debug("ONDEMAND BEHAVIOR STARTING...");
            }
            for (int i = 0; i < ondemandContainers_.length; i++) {
                ondemandContainers_[i].start();
            }
            if (logger_.isDebugEnabled()) {
                logger_.debug("ONDEMAND BEHAVIOR STARTED");
            }
        }
    }

    S2Container getContainer() {
        return SingletonPluggableContainerFactory.getRootContainer();
    }

    public synchronized void stop() {
        if (logger_.isDebugEnabled()) {
            logger_.debug("OndemandBehavior's stop() method called");
        }
        if (!hotdeployEnabled_) {
            return;
        }

        if (--counter_ == 0) {
            if (logger_.isDebugEnabled()) {
                logger_.debug("ONDEMAND BEHAVIOR STOPPING...");
            }
            DisposableUtil.dispose();

            for (int i = 0; i < ondemandContainers_.length; i++) {
                ondemandContainers_[i].stop();
            }

            if (logger_.isDebugEnabled()) {
                logger_.debug("ONDEMAND BEHAVIOR STOPPED");
            }
        } else if (counter_ < 0) {
            throw new IllegalStateException("Unbalanced stop() calling");
        }
    }

    protected ComponentDef getComponentDef(S2Container container, Object key) {

        ComponentDef cd = super.getComponentDef(container, key);
        if (cd != null) {
            return cd;
        }
        return findComponentDefFromOndemandS2Containers(container, key);
    }

    protected ComponentDef findComponentDefFromOndemandS2Containers(
            S2Container container, Object key) {

        LocalOndemandS2Container[] localOndemandS2Containers = (LocalOndemandS2Container[]) container
                .findAllComponents(LocalOndemandS2Container.class);
        for (int i = 0; i < localOndemandS2Containers.length; i++) {
            ComponentDef cd = localOndemandS2Containers[i]
                    .findComponentDef(key);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }
}
