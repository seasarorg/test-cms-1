package org.seasar.cms.pluggable;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory.DefaultProvider;

public class PluggableProvider extends DefaultProvider {
    private static ThreadLocal usingPluggableRoot_ = new ThreadLocal();

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
                final String realPath = pathResolver.resolvePath(parent
                        .getPath(), path);
                S2Container child;
                if (root.hasDescendant(realPath)) {
                    child = root.getDescendant(realPath);
                    parent.include(child);
                } else {
                    child = super.include(parent, path);
                    root.registerDescendant(child);
                }
                return child;
            }
        } else {
            return super.include(parent, path);
        }
    }
}
