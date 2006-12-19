package org.seasar.cms.classbuilder.impl;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.util.S2ContainerPreparerUtils;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.assembler.AutoConstructorAssembler;


public class PluggableAutoConstructorAssembler extends AutoConstructorAssembler
{
    public PluggableAutoConstructorAssembler(ComponentDef componentDef)
    {
        super(componentDef);
    }


    @Override
    protected Object doAssemble()
    {
        Object component = null;

        S2ContainerPreparer preparer = S2ContainerPreparerUtils
            .getPreparer(getComponentDef());
        if (preparer != null) {
            component = preparer.newInstance(getComponentDef()
                .getComponentName());
        }
        if (component == null) {
            component = super.doAssemble();
        }
        return component;
    }
}
