package org.seasar.cms.classbuilder.impl;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.util.S2ContainerPreparerUtils;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.IllegalMethodRuntimeException;
import org.seasar.framework.container.assembler.DefaultInitMethodAssembler;


public class PluggableInitMethodAssembler extends DefaultInitMethodAssembler
{
    public PluggableInitMethodAssembler(ComponentDef componentDef)
    {
        super(componentDef);
    }


    @Override
    public void assemble(Object component)
        throws IllegalMethodRuntimeException
    {
        super.assemble(component);

        S2ContainerPreparer preparer = S2ContainerPreparerUtils
            .getPreparer(getComponentDef());
        if (preparer == null) {
            return;
        }
        try {
            preparer
                .initialize(component, getComponentDef().getComponentName());
        } catch (Throwable t) {
            throw new IllegalMethodRuntimeException(
                getComponentClass(component), getComponentDef()
                    .getComponentName(), t);
        }
    }
}
