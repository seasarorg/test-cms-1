package org.seasar.cms.pluggable;

import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.MetaDef;
import org.seasar.framework.container.PropertyDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.SimpleComponentDef;
import org.seasar.framework.container.util.BindingUtil;
import org.seasar.framework.container.util.Traversal;
import org.seasar.framework.container.util.Traversal.S2ContainerHandler;

public class DelayedPropertySetter {

    private static final String NAME_DELAYED = "delayed";

    private S2Container container_;

    private S2ContainerHandler handler_ = new S2ContainerHandler() {
        public Object processContainer(S2Container container) {
            setDelayedProperties(container);
            return null;
        }
    };

    public void setAll() {

        Traversal.forEachContainer(container_, handler_, false);
    }

    public void setDelayedProperties(S2Container container) {

        int size = container.getComponentDefSize();
        for (int i = 0; i < size; i++) {
            setDelayedProperties(container.getComponentDef(i));
        }
    }

    void setDelayedProperties(ComponentDef componentDef) {

        if (componentDef instanceof SimpleComponentDef) {
            // SimpleComponentDefは大抵のメソッドがUnsupportedOperationException
            // なので、何もしない。
            return;
        }
        Object component = componentDef.getComponent();
        BeanDesc beanDesc = BindingUtil.getBeanDesc(componentDef, component);
        int size = componentDef.getPropertyDefSize();
        for (int i = 0; i < size; i++) {
            setDelayedProperties(componentDef, componentDef.getPropertyDef(i),
                beanDesc, component);
        }
    }

    void setDelayedProperties(ComponentDef componentDef,
        PropertyDef propertyDef, BeanDesc beanDesc, Object component) {

        int size = propertyDef.getMetaDefSize();
        for (int i = 0; i < size; i++) {
            MetaDef metaDef = propertyDef.getMetaDef(i);
            if (!NAME_DELAYED.equals(metaDef.getName())) {
                continue;
            }
            PropertyDesc propertyDesc = beanDesc.getPropertyDesc(propertyDef
                .getPropertyName());
            if (!propertyDesc.hasWriteMethod()) {
                continue;
            }
            Object value = metaDef.getValue();
            if (value == null) {
                continue;
            }
            propertyDesc.setValue(component, value);
        }
    }

    public void setContainer(S2Container container) {
        container_ = container;
    }
}
