package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.util.ClassBuilderUtils;
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

        S2ContainerPreparer preparer = ClassBuilderUtils
            .getPreparer(getComponentDef());
        if (preparer != null) {
            component = newInstance(preparer, getComponentDef());
        }
        if (component == null) {
            component = super.doAssemble();
        }
        return component;
    }


    Object newInstance(S2ContainerPreparer preparer, ComponentDef componentDef)
    {
        Method method = ClassBuilderUtils.findMethod(
            preparer.getClass(), componentDef.getComponentName(),
            ClassS2ContainerBuilder.METHODPREFIX_NEW);
        if (method != null) {
            try {
                if (method.getParameterTypes().length == 0) {
                    return method.invoke(preparer, new Object[0]);
                } else {
                    // Aspectをかけたコンポーネントの場合はconcreateClassからオブジェクトを
                    // 生成する必要がある。その場合は引数としてconcreteClassを受け取るタイプ
                    // のnewメソッドが利用される。
                    return method.invoke(preparer, new Object[] { componentDef
                        .getConcreteClass() });
                }
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
