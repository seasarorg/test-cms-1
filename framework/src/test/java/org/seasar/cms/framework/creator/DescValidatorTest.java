package org.seasar.cms.framework.creator;

import junit.framework.TestCase;

import org.seasar.cms.framework.creator.impl.TypeDescImpl;

public class DescValidatorTest extends TestCase {

    private ClassDescSet classDescSet_ = new ClassDescSet();

    public void testIsValid1() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl(TypeDesc.TYPE_VOID);

        // ## Act ##
        boolean actual = DescValidator.isValid(target, classDescSet_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid2() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("int");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, classDescSet_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid3() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("String");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, classDescSet_);

        // ## Assert ##
        assertFalse(actual);
    }

    public void testIsValid4() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.String");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, classDescSet_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid5() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.String[]");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, classDescSet_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid6() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.Hoehoe");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, classDescSet_);

        // ## Assert ##
        assertFalse(actual);
    }
}
