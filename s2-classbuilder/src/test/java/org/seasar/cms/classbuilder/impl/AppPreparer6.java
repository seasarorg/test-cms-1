package org.seasar.cms.classbuilder.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.annotation.Component;
import org.seasar.cms.classbuilder.annotation.ManualBindingProperties;
import org.seasar.framework.container.annotation.tiger.Aspect;
import org.seasar.framework.container.annotation.tiger.InstanceType;


public class AppPreparer6 extends S2ContainerPreparer
{
    public void defineGuu(GuuImpl guu)
    {
    }


    @ManualBindingProperties("guu2")
    public void defineHoe4(Hoe4 component)
    {
    }


    @Component(instance = InstanceType.PROTOTYPE)
    public void defineList(ArrayList component)
    {
    }


    @Aspect("interceptor")
    public void defineMap(HashMap component)
    {
    }


    public void defineInterceptor(HoeInterceptor component)
    {
    }
}
