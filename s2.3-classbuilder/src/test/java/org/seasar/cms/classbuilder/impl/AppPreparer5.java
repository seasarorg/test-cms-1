package org.seasar.cms.classbuilder.impl;

import org.seasar.cms.classbuilder.S2ContainerPreparer;


public class AppPreparer5 extends S2ContainerPreparer
{
    @Override
    public void include()
    {
        include(ChildPreparer1.class);
    }


    public void defineHoe3(Hoe3 component)
    {
        component.setFuga3(getComponent(Fuga3.class));
    }
}
