package org.seasar.cms.pluggable;

import junit.framework.TestCase;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.impl.S2ContainerImpl;

public class DelayedPropertySetterTest extends TestCase {

    private DelayedPropertySetter target_ = new DelayedPropertySetter();

    public void testSetDelayedProperties_S2Container() throws Exception {

        S2Container container = S2ContainerFactory.create(getClass().getName()
            .replace('.', '/')
            + "_delayed.dicon");
        container.init();
        Bean bean2 = (Bean) container.getComponent("bean2");
        assertNull(bean2.getValue());
        assertNull(container.getComponentDef(1).getPropertyDef(0).getValue());

        target_.setDelayedProperties(container);

        assertEquals("value", bean2.getValue());
    }

    public void testSetAll() throws Exception {

        S2Container root = new S2ContainerImpl();
        S2Container container = S2ContainerFactory.include(root, getClass()
            .getName().replace('.', '/')
            + "_delayed.dicon");
        root.init();
        Bean bean2 = (Bean) container.getComponent("bean2");
        assertNull(bean2.getValue());
        assertNull(container.getComponentDef(1).getPropertyDef(0).getValue());

        target_.setContainer(root);
        target_.setAll();

        assertEquals("value", bean2.getValue());
    }
}
