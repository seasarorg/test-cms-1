package org.seasar.cms.classbuilder.impl;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ConstructorAssembler;
import org.seasar.framework.container.MethodAssembler;
import org.seasar.framework.container.PropertyAssembler;
import org.seasar.framework.container.assembler.AssemblerFactory.DefaultProvider;


public class ClassAssemblerProvider extends DefaultProvider
{
    @Override
    public ConstructorAssembler createAutoConstructorAssembler(ComponentDef cd)
    {
        return new ClassAutoConstructorAssembler(cd);
    }


    @Override
    public PropertyAssembler createAutoPropertyAssembler(ComponentDef cd)
    {
        return new ClassAutoPropertyAssembler(cd);
    }


    @Override
    public MethodAssembler createInitMethodAssembler(ComponentDef cd)
    {
        return new ClassInitMethodAssembler(cd);
    }


    @Override
    public MethodAssembler createDestroyMethodAssembler(ComponentDef cd)
    {
        return new ClassDestroyMethodAssembler(cd);
    }
}
