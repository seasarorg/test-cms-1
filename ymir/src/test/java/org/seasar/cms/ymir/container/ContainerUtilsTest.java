package org.seasar.cms.ymir.container;

import junit.framework.TestCase;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class ContainerUtilsTest extends TestCase {

    public void testFindAscendantComponents() throws Exception {

        // ## Arrange ##
        S2Container root = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/ContainerUtilsTest_root.dicon");
        S2Container leaf = root.getChild(0).getChild(0);

        // ## Act ##
        Object[] listeners = ContainerUtils.findAscendantComponents(leaf,
            Listener.class);

        // ## Assert ##
        assertEquals(1, listeners.length);
    }
}
