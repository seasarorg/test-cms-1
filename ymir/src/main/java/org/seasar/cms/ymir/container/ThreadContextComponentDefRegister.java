package org.seasar.cms.ymir.container;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.util.Traversal;
import org.seasar.framework.container.util.Traversal.S2ContainerHandler;

public class ThreadContextComponentDefRegister {

    private ThreadContextComponentDefFactory[] factories_ = new ThreadContextComponentDefFactory[0];

    private S2Container container_;

    private ThreadContext context_;

    public void registerComponentDefs() {
        for (int i = 0; i < factories_.length; i++) {
            context_.register(new ThreadLocalComponentDef(factories_[i]
                .getComponentClass(), factories_[i].getComponentName()));
        }
        Traversal.forEachContainer(container_.getRoot(),
            new S2ContainerHandler() {
                public Object processContainer(S2Container container) {
                    for (int i = 0; i < factories_.length; i++) {
                        container.register(factories_[i].newInstance());
                    }
                    return null;
                }
            });
    }

    public void destroy() {
        factories_ = null;
        container_ = null;
        context_ = null;
    }

    public void setComponentDefs(ThreadContextComponentDefFactory[] factories) {
        factories_ = factories;
    }

    public void setContainer(S2Container container) {
        container_ = container;
    }

    public void setThreadContext(ThreadContext context) {
        context_ = context;
    }
}