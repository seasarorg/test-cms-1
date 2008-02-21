package org.seasar.cms.classbuilder.impl;

import org.seasar.cms.classbuilder.S2ContainerPreparer;


public class AppPreparer12 extends S2ContainerPreparer
{
    public void defineHoe(HoeImpl component)
    {
        component.setName("defined");
    }


    public void destroyHoe(HoeImpl component)
    {
        component.setName("destroyed");
    }
}
