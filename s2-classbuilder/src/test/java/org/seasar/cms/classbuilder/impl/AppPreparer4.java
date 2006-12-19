package org.seasar.cms.classbuilder.impl;

import org.seasar.cms.classbuilder.S2ContainerPreparer;


public class AppPreparer4 extends S2ContainerPreparer
{
    public void defineHoe(Hoe2 component)
    {
        component.setFuga2(getComponent(Fuga2.class));
    }


    public void defineFuga(Fuga2 component)
    {
        component.setHoe2(getComponent(Hoe2.class));
    }
}
