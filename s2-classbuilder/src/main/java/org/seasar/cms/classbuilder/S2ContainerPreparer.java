package org.seasar.cms.classbuilder;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;


public class S2ContainerPreparer
{
    private S2Container container_;


    public final void setContainer(S2Container container)
    {
        container_ = container;
    }


    public final S2Container getContainer()
    {
        return container_;
    }


    public void include()
    {
    }


    @SuppressWarnings("unchecked")
    public final <T> T getComponent(Class<T> key)
    {
        return (T)container_.getComponent(key);
    }


    public final Object getComponent(Object key)
    {
        return container_.getComponent(key);
    }


    public final void include(Class<? extends S2ContainerPreparer> preparer)
    {
        include(preparer.getName().replace('.', '/').concat(".class"));
    }


    public final void include(String path)
    {
        S2ContainerFactory.include(container_, path);
    }
}
