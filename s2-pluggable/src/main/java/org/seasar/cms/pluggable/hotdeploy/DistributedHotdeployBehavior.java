package org.seasar.cms.pluggable.hotdeploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.cms.pluggable.util.HotdeployEventUtils;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerBehavior.DefaultProvider;
import org.seasar.framework.util.DisposableUtil;

public class DistributedHotdeployBehavior extends DefaultProvider {
    private boolean hotdeploy_;

    private boolean dynamic_;

    private LocalHotdeployS2Container[] localHotdeployS2Containers_;

    private int counter_ = 0;

    private Log log_ = LogFactory.getLog(DistributedHotdeployBehavior.class);

    public void init(boolean hotdeploy, boolean dynamic) {
        hotdeploy_ = hotdeploy;
        dynamic_ = dynamic;
        initializeLocalHotdeployS2Containers();
    }

    void initializeLocalHotdeployS2Containers() {
        S2Container container = getContainer();

        // LocalHotdeployS2Containerを集める。
        ComponentDef[] componentDefs = container
                .findAllComponentDefs(LocalHotdeployS2Container.class);
        localHotdeployS2Containers_ = new LocalHotdeployS2Container[componentDefs.length];
        for (int i = 0; i < componentDefs.length; i++) {
            ComponentDef cd = componentDefs[i];
            LocalHotdeployS2Container localContainer = (LocalHotdeployS2Container) cd
                    .getComponent();

            // このLocalHotdeployS2Containerが登録されているコンテナから見える
            // HotdeployListenerを登録する。
            HotdeployListener[] listeners = (HotdeployListener[]) cd
                    .getContainer().findAllComponents(HotdeployListener.class);
            for (int j = 0; j < listeners.length; j++) {
                HotdeployListener listener = listeners[j];
                if (listener instanceof LocalHotdeployS2Container
                        && listener != localContainer) {
                    // このLocalHotdeployS2Containerが登録されているコンテナから見えても、
                    // 自分以外のLocalHotdeployS2Containerは登録しない。
                    continue;
                }
                localContainer.addHotdeployListener(listener);
            }
            localContainer.init(hotdeploy_, dynamic_);
            localHotdeployS2Containers_[i] = localContainer;
        }
    }

    public LocalHotdeployS2Container[] getLocalHotdeployS2Containers() {
        return localHotdeployS2Containers_;
    }

    public void destroy() {
        if (localHotdeployS2Containers_ != null) {
            for (int i = 0; i < localHotdeployS2Containers_.length; i++) {
                localHotdeployS2Containers_[i].destroy();
            }
        }
        hotdeploy_ = false;
    }

    public synchronized void start() {
        if (log_.isDebugEnabled()) {
            log_.debug("HotdeployBehavior's start() method called");
        }
        if (!hotdeploy_ && !dynamic_) {
            return;
        }

        if (counter_++ == 0) {
            if (log_.isDebugEnabled()) {
                log_.debug("HOTDEPLOY BEHAVIOR STARTING...");
            }
            for (int i = 0; i < localHotdeployS2Containers_.length; i++) {
                localHotdeployS2Containers_[i].start();
            }
            HotdeployEventUtils.start();
            if (log_.isDebugEnabled()) {
                log_.debug("HOTDEPLOY BEHAVIOR STARTED");
            }
        }
    }

    S2Container getContainer() {
        return SingletonPluggableContainerFactory.getRootContainer();
    }

    public synchronized void stop() {
        if (log_.isDebugEnabled()) {
            log_.debug("HotdeployBehavior's stop() method called");
        }
        if (!hotdeploy_ && !dynamic_) {
            return;
        }

        if (--counter_ == 0) {
            if (log_.isDebugEnabled()) {
                log_.debug("HOTDEPLOY BEHAVIOR STOPPING...");
            }
            HotdeployEventUtils.stop();
            DisposableUtil.dispose();

            for (int i = 0; i < localHotdeployS2Containers_.length; i++) {
                localHotdeployS2Containers_[i].stop();
            }

            if (log_.isDebugEnabled()) {
                log_.debug("HOTDEPLOY BEHAVIOR STOPPED");
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
        return findComponentDefFromHotdeployS2Containers(container, key);
    }

    protected ComponentDef findComponentDefFromHotdeployS2Containers(
            S2Container container, Object key) {
        LocalHotdeployS2Container[] localHotdeployS2Containers = (LocalHotdeployS2Container[]) container
                .findAllComponents(LocalHotdeployS2Container.class);
        for (int i = 0; i < localHotdeployS2Containers.length; i++) {
            ComponentDef cd = localHotdeployS2Containers[i]
                    .findComponentDef(key);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }
}
