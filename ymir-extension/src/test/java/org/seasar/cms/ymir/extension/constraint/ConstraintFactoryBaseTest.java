package org.seasar.cms.ymir.extension.constraint;

import junit.framework.TestCase;

public class ConstraintFactoryBaseTest extends TestCase {

    private ConstraintFactoryBase target_ = new ConstraintFactoryBase() {
    };

    public void testAdd() throws Exception {

        String[] actual = target_.add(new String[] { "a", "b" }, null);

        assertEquals(2, actual.length);
        assertEquals("a", actual[0]);
        assertEquals("b", actual[1]);
    }

    public void testAdd2() throws Exception {

        String[] actual = target_.add(new String[] { "a", "b" }, "c");

        assertEquals(3, actual.length);
        assertEquals("a", actual[0]);
        assertEquals("b", actual[1]);
        assertEquals("c", actual[2]);
    }
}
