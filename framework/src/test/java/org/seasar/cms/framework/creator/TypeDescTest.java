package org.seasar.cms.framework.creator;

import junit.framework.TestCase;

public class TypeDescTest extends TestCase {

    public void testIsValid1() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDesc(TypeDesc.TYPE_VOID);

        // ## Act ##
        boolean actual = target.isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid2() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDesc("int");

        // ## Act ##
        boolean actual = target.isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid3() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDesc("String");

        // ## Act ##
        boolean actual = target.isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid4() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDesc("java.lang.String");

        // ## Act ##
        boolean actual = target.isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid5() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDesc("java.lang.String[]");

        // ## Act ##
        boolean actual = target.isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid6() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDesc("java.lang.Hoehoe");

        // ## Act ##
        boolean actual = target.isValid();

        // ## Assert ##
        assertFalse(actual);
    }
}
