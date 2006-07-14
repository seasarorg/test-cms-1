package org.seasar.cms.ymir.container;

import junit.framework.TestCase;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class ContainerUtilsTest extends TestCase {

    public void testFindAllComponents() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/ContainerUtilsTest_root.dicon");

        // ## Act ##
        Object[] listeners = ContainerUtils.findAllComponents(container,
            Listener.class);

        // ## Assert ##
        assertEquals(2, listeners.length);
        assertSame(Listener.class, listeners.getClass().getComponentType());
        assertTrue(listeners[0] instanceof OneListener);
    }
}
