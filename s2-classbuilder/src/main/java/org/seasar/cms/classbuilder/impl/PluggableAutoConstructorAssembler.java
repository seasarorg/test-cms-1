package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            component = newInstance(preparer, getComponentDef()
                .getComponentName());
        }
        if (component == null) {
            component = super.doAssemble();
        }
        return component;
    }


    Object newInstance(S2ContainerPreparer preparer, String componentName)
    {
        Method method = S2ContainerPreparerUtils.findMethod(
            preparer.getClass(), componentName,
            ClassS2ContainerBuilder.METHODPREFIX_NEW);
        if (method != null) {
            try {
                return method.invoke(preparer, new Object[0]);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(
                    "Can't invoke method for instanciating component: "
                        + method.getName(), ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(
                    "Can't invoke method for instanciating component: "
                        + method.getName(), ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(
                    "Can't invoke method for instanciating component: "
                        + method.getName(), ex);
            }
        } else {
            return null;
        }
    }
}
