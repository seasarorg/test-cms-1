package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            initializeComponent(preparer, component, getComponentDef()
                .getComponentName());
        } catch (Throwable t) {
            throw new IllegalMethodRuntimeException(
                getComponentClass(component), getComponentDef()
                    .getComponentName(), t);
        }
    }


    void initializeComponent(S2ContainerPreparer preparer, Object component,
        String componentName)
    {
        Method method = S2ContainerPreparerUtils.findMethod(
            preparer.getClass(), componentName,
            ClassS2ContainerBuilder.METHODPREFIX_DEFINE);
        if (method != null) {
            try {
                method.invoke(preparer, new Object[] { component });
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Can't initialize: " + componentName);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Can't initialize: " + componentName);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException("Can't initialize: " + componentName);
            }
        }
    }
}
