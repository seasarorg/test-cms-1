package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.annotation.ManualBindingProperties;
import org.seasar.cms.classbuilder.util.ClassBuilderUtils;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.container.BindingTypeDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.PropertyDef;
import org.seasar.framework.container.assembler.AbstractPropertyAssembler;
import org.seasar.framework.container.assembler.BindingTypeDefFactory;

public class ClassAutoPropertyAssembler extends AbstractPropertyAssembler {
    public ClassAutoPropertyAssembler(ComponentDef componentDef) {
        super(componentDef);
    }

    public void assemble(Object component) {
        if (component == null) {
            return;
        }

        BeanDesc beanDesc = getBeanDesc(component);
        ComponentDef cd = getComponentDef();
        int size = cd.getPropertyDefSize();
        Set<String> names = new HashSet<String>();
        S2ContainerPreparer preparer = ClassBuilderUtils
                .getPreparer(getComponentDef());
        if (preparer != null) {
            names.addAll(Arrays.asList(getManualBindingProperties(preparer,
                    getComponentDef().getComponentName())));
        }

        size = beanDesc.getPropertyDescSize();
        for (int i = 0; i < size; ++i) {
            PropertyDesc propDesc = beanDesc.getPropertyDesc(i);
            String propName = propDesc.getPropertyName();
            if (cd.hasPropertyDef(propName)) {
                PropertyDef propDef = cd.getPropertyDef(propName);
                BindingTypeDef bindingTypeDef = propDef.getBindingTypeDef();
                bindingTypeDef.bind(cd, propDef, propDesc, component);
            } else if (!names.contains(propName)) {
                BindingTypeDefFactory.SHOULD.bind(getComponentDef(), null,
                        propDesc, component);
            }
        }
    }

    String[] getManualBindingProperties(S2ContainerPreparer preparer,
            String componentName) {
        Method method = ClassBuilderUtils.findMethod(preparer.getClass(),
                componentName, ClassS2ContainerBuilder.METHODPREFIX_DEFINE);
        if (method != null) {
            ManualBindingProperties annotation = method
                    .getAnnotation(ManualBindingProperties.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        return new String[0];
    }
}
