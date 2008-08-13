package org.seasar.framework.container.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.util.Traversal.S2ContainerHandler;
import org.seasar.framework.util.ArrayUtil;

@SuppressWarnings("unchecked")
public class Traversal24 {
    /**
     * 親の {@link S2Container}をトラバースします。
     * 
     * @param container
     * @param handler
     * @return 処理した結果
     * @see #forEachParentContainer(S2Container,
     *      org.seasar.framework.container.util.Traversal.S2ContainerHandler,
     *      boolean, Set)
     */
    public static Object forEachParentContainer(final S2Container container,
            final S2ContainerHandler handler) {
        return forEachParentContainer(container, handler, true, new HashSet());
    }

    /**
     * 親の {@link S2Container}をトラバースします。
     * 
     * @param container
     * @param handler
     * @param childFirst
     * @return 処理した結果
     * @see #forEachParentContainer(S2Container,
     *      org.seasar.framework.container.util.Traversal.S2ContainerHandler,
     *      boolean, Set)
     */
    public static Object forEachParentContainer(final S2Container container,
            final S2ContainerHandler handler, final boolean childFirst) {
        return forEachParentContainer(container, handler, childFirst,
                new HashSet());
    }

    /**
     * 親の {@link S2Container}をトラバースします。
     * 
     * @param container
     * @param handler
     * @param childFirst
     * @param processed
     * @return 処理した結果
     */
    protected static Object forEachParentContainer(final S2Container container,
            final S2ContainerHandler handler, final boolean childFirst,
            final Set processed) {
        return forEachParentContainer(container, handler, childFirst,
                processed, new ContainerGraph(container));
    }

    @SuppressWarnings("unchecked")
    protected static Object forEachParentContainer(final S2Container container,
            final S2ContainerHandler handler, final boolean childFirst,
            final Set processed, final ContainerGraph graph) {
        if (childFirst) {
            final Object result = handler.processContainer(container);
            if (result != null) {
                return result;
            }
        }

        S2Container[] parents = graph.getParents(container);
        for (S2Container parent : parents) {
            if (processed.contains(parent)) {
                continue;
            }
            processed.add(parent);
            final Object result = forEachParentContainer(parent, handler,
                    childFirst, processed, graph);
            if (result != null) {
                return result;
            }
        }
        if (!childFirst) {
            return handler.processContainer(container);
        }
        return null;
    }

    static class ContainerGraph {
        private Map<S2Container, S2Container[]> parentLink_ = new HashMap<S2Container, S2Container[]>();

        ContainerGraph(S2Container container) {
            register(container.getRoot(), container);
        }

        void register(S2Container parent, S2Container leaf) {
            if (parent == leaf) {
                return;
            }

            synchronized (parent) {
                int size = parent.getChildSize();
                for (int i = 0; i < size; i++) {
                    S2Container child = parent.getChild(i);
                    S2Container[] parents = parentLink_.get(child);
                    if (parents == null) {
                        parentLink_.put(child, new S2Container[] { parent });
                        register(child, leaf);
                    } else {
                        parentLink_.put(child, (S2Container[]) ArrayUtil.add(
                                parents, parent));
                    }
                }
            }
        }

        public S2Container[] getParents(S2Container child) {
            S2Container[] parents = parentLink_.get(child);
            if (parents == null) {
                return new S2Container[0];
            } else {
                return parents;
            }
        }
    }
}
