package org.seasar.framework.container.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerImpl;
import org.seasar.framework.container.util.Traversal24.ContainerGraph;

public class Traversal24Test extends TestCase {
    private S2Container root_ = new S2ContainerImpl();

    private S2Container node1_ = new S2ContainerImpl();

    private S2Container node2_ = new S2ContainerImpl();

    private S2Container node3_ = new S2ContainerImpl();

    private S2Container node4_ = new S2ContainerImpl();

    private S2Container node5_ = new S2ContainerImpl();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        root_.setPath("root");
        node1_.setPath("node1");
        node2_.setPath("node2");
        node3_.setPath("node3");
        node4_.setPath("node4");
        node5_.setPath("node5");

        root_.include(node1_);
        root_.include(node2_);
        node1_.include(node3_);
        node2_.include(node3_);
        node3_.include(node4_);
        node3_.include(node5_);
    }

    public void testContainerGraph() throws Exception {
        ContainerGraph target = new ContainerGraph(node3_);
        S2Container[] actual = target.getParents(node3_);

        assertNotNull(actual);
        assertEquals(2, actual.length);
        assertSame(node1_, actual[0]);
        assertSame(node2_, actual[1]);

        actual = target.getParents(node2_);
        assertNotNull(actual);
        assertEquals(1, actual.length);
        assertSame(root_, actual[0]);

        actual = target.getParents(node1_);
        assertNotNull(actual);
        assertEquals(1, actual.length);
        assertSame(root_, actual[0]);

        actual = target.getParents(root_);
        assertNotNull(actual);
        assertEquals(0, actual.length);
    }

    public void testForEachParentContainer() throws Exception {
        final List<String> actual = new ArrayList<String>();

        Traversal24.forEachParentContainer(node3_,
                new Traversal.S2ContainerHandler() {
                    public Object processContainer(S2Container container) {
                        actual.add(container.getPath());
                        return null;
                    }
                });

        assertEquals(4, actual.size());
        int idx = 0;
        assertEquals("node3", actual.get(idx++));
        assertEquals("node1", actual.get(idx++));
        assertEquals("root", actual.get(idx++));
        assertEquals("node2", actual.get(idx++));
    }
}
