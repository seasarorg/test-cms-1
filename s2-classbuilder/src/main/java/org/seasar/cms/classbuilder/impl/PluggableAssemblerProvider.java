package org.seasar.cms.classbuilder.impl;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ConstructorAssembler;
import org.seasar.framework.container.MethodAssembler;
import org.seasar.framework.container.PropertyAssembler;
import org.seasar.framework.container.assembler.AssemblerFactory.DefaultProvider;


public class PluggableAssemblerProvider extends DefaultProvider
{
    @Override
    public ConstructorAssembler createAutoConstructorAssembler(ComponentDef cd)
    {
        return new PluggableAutoConstructorAssembler(cd);
    }


    @Override
    public PropertyAssembler createAutoPropertyAssembler(ComponentDef cd)
    {
        return new PluggableAutoPropertyAssembler(cd);
    }


    @Override
    public MethodAssembler createInitMethodAssembler(ComponentDef cd)
    {
        return new PluggableInitMethodAssembler(cd);
    }
}
