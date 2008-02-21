package org.seasar.cms.classbuilder.impl;

import org.seasar.cms.classbuilder.S2ContainerPreparer;


public class AppPreparer10_hoe extends S2ContainerPreparer
{
    public void defineHoe(HoeImpl component)
    {
        component.setName("redefined");
    }
}
