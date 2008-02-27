package org.seasar.cms.pluggable;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory.DefaultProvider;

public class PluggableProvider extends DefaultProvider {
    private static ThreadLocal<Boolean> usingPluggableRoot_ = new ThreadLocal<Boolean>();

    public static boolean isUsingPluggableRoot() {
        Boolean usingPluggableRoot = (Boolean) usingPluggableRoot_.get();
        if (usingPluggableRoot != null) {
            return usingPluggableRoot.booleanValue();
        } else {
            return false;
        }
    }

    public static void setUsingPluggableRoot(boolean usingPluggableRoot) {
        usingPluggableRoot_.set(Boolean.valueOf(usingPluggableRoot));
    }

    public S2Container include(S2Container parent, String path) {
        if (isUsingPluggableRoot()) {
            S2Container root = SingletonPluggableContainerFactory
                    .getRootContainer();
            synchronized (root) {
                final String realPath = pathResolver_.resolvePath(parent
                        .getPath(), path);
                S2Container child;
                if (root.hasDescendant(realPath)) {
                    child = root.getDescendant(realPath);
                    parent.include(child);
                } else {
                    child = includeWithExpanding(parent, path);
                    root.registerDescendant(child);
                }
                return child;
            }
        } else {
            return includeWithExpanding(parent, path);
        }
    }

    S2Container includeWithExpanding(S2Container parent, String path) {
        return SingletonPluggableContainerFactory.getInstance()
                .processExpanding(super.include(parent, path));
    }
}
