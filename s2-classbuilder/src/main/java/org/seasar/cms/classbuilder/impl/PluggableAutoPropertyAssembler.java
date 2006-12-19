package org.seasar.cms.classbuilder.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.cms.classbuilder.util.S2ContainerPreparerUtils;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.IllegalPropertyRuntimeException;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.container.AccessTypeDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.PropertyDef;
import org.seasar.framework.container.assembler.AbstractPropertyAssembler;
import org.seasar.framework.container.assembler.BindingTypeDefFactory;


public class PluggableAutoPropertyAssembler extends AbstractPropertyAssembler
{
    public PluggableAutoPropertyAssembler(ComponentDef componentDef)
    {
        super(componentDef);
    }


    public void assemble(Object component)
        throws IllegalPropertyRuntimeException
    {
        if (component == null) {
            return;
        }

        BeanDesc beanDesc = getBeanDesc(component);
        ComponentDef cd = getComponentDef();
        int size = cd.getPropertyDefSize();
        Set<String> names = new HashSet<String>();
        for (int i = 0; i < size; ++i) {
            PropertyDef propDef = cd.getPropertyDef(i);
            AccessTypeDef accessTypeDef = propDef.getAccessTypeDef();
            accessTypeDef.bind(cd, propDef, component);
            String propName = propDef.getPropertyName();
            names.add(propName);
        }

        S2ContainerPreparer preparer = S2ContainerPreparerUtils.getPreparer(getComponentDef());
        if (preparer != null) {
            names.addAll(Arrays.asList(preparer
                .getManualBindingProperties(getComponentDef()
                    .getComponentName())));
        }

        if (cd.isExternalBinding()) {
            bindExternally(beanDesc, cd, component, names);
        }
        size = beanDesc.getPropertyDescSize();
        for (int i = 0; i < size; ++i) {
            PropertyDesc propDesc = beanDesc.getPropertyDesc(i);
            String propName = propDesc.getPropertyName();
            if (!names.contains(propName)) {
                BindingTypeDefFactory.SHOULD.bind(getComponentDef(), null,
                    propDesc, component);
            }
        }
    }
}
