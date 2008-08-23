package org.seasar.cms.pluggable;

import java.util.Stack;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory.DefaultProvider;

public class PluggableProvider extends DefaultProvider {
    private static ThreadLocal<Stack<S2Container>> root_ = new ThreadLocal<Stack<S2Container>>();

    public static S2Container getRoot() {
        Stack<S2Container> stack = root_.get();
        if (stack == null) {
            return null;
        } else {
            return stack.peek();
        }
    }

    public static void registerRoot(S2Container root) {
        Stack<S2Container> stack = root_.get();
        if (root != null) {
            if (stack == null) {
                stack = new Stack<S2Container>();
                root_.set(stack);
            }
            stack.push(root);
        } else {
            stack.pop();
            if (stack.isEmpty()) {
                root_.set(null);
            }
        }
    }

    public S2Container include(S2Container parent, String path) {
        S2Container root = getRoot();
        if (root != null) {
            synchronized (root) {
                final String realPath = pathResolver.resolvePath(parent
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
