package org.seasar.cms.pluggable.hotdeploy;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerBehavior.DefaultProvider;
import org.seasar.framework.log.Logger;

public class DistributedHotdeployBehavior extends DefaultProvider {

    private LocalHotdeployS2Container[] localHotdeployS2Containers_;

    private Logger logger_ = Logger.getLogger(getClass());

    public void init(boolean hotdeployEnabled) {
    }

    public void destroy() {
    }

    public synchronized void start() {
        if (logger_.isDebugEnabled()) {
            logger_.debug("HotdeployBehavior's start() method called");
        }
    }

    S2Container getContainer() {
        return SingletonPluggableContainerFactory.getRootContainer();
    }

    public synchronized void stop() {
        if (logger_.isDebugEnabled()) {
            logger_.debug("HotdeployBehavior's stop() method called");
        }
    }
}
