package org.seasar.cms.ymir.container;

import junit.framework.TestCase;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class ContainerUtilsTest extends TestCase {

    public void testFindDescendantComponents() throws Exception {

        // ## Arrange ##
        S2Container root = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/ContainerUtilsTest_root.dicon");
        S2Container anotherRoot = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/ContainerUtilsTest_anotherRoot.dicon");

        root.include(anotherRoot);
        anotherRoot.setRoot(anotherRoot);

        // ## Act ##
        Object[] listeners = ContainerUtils.findDescendantComponents(root,
            Listener.class);

        // ## Assert ##
        assertEquals(2, listeners.length);
        assertSame(Listener.class, listeners.getClass().getComponentType());
        assertTrue(listeners[0] instanceof OneListener);
    }

    public void testFindAllComponents() throws Exception {

        // ## Arrange ##
        S2Container root = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/ContainerUtilsTest_root.dicon");
        S2Container anotherRoot = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/ContainerUtilsTest_anotherRoot.dicon");

        root.include(anotherRoot);
        anotherRoot.setRoot(anotherRoot);

        // ## Act ##
        Object[] listeners = ContainerUtils.findAllComponents(root,
            Listener.class);

        // ## Assert ##
        assertEquals(4, listeners.length);
        assertSame(Listener.class, listeners.getClass().getComponentType());
    }
}
