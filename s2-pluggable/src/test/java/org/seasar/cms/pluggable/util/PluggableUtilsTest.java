package org.seasar.cms.pluggable.util;

import junit.framework.TestCase;

import org.seasar.cms.pluggable.Listener;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class PluggableUtilsTest extends TestCase {

    public void testFindAscendantComponents() throws Exception {

        // ## Arrange ##
        S2Container root = S2ContainerFactory.create(getClass().getName()
            .replace('.', '/')
            + "_root.dicon");
        S2Container leaf = root.getChild(0).getChild(0);

        // ## Act ##
        Object[] listeners = PluggableUtils.findAscendantComponents(leaf,
            Listener.class);

        // ## Assert ##
        assertEquals(1, listeners.length);
    }
}
