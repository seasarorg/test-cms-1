package org.seasar.cms.ymir.extension.creator;

import junit.framework.TestCase;

import org.seasar.cms.ymir.extension.creator.impl.TypeDescImpl;

public class DescValidatorTest extends TestCase {

    private ClassDescSet classDescSet_ = new ClassDescSet();

    public void testValidate1() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl(TypeDesc.TYPE_VOID);

        // ## Act ##
        boolean actual = DescValidator.validate(target, classDescSet_)
                .isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testValidate2() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("int");

        // ## Act ##
        boolean actual = DescValidator.validate(target, classDescSet_)
                .isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testValidate3() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("String");

        // ## Act ##
        boolean actual = DescValidator.validate(target, classDescSet_)
                .isValid();

        // ## Assert ##
        assertFalse(actual);
    }

    public void testValidate4() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.String");

        // ## Act ##
        boolean actual = DescValidator.validate(target, classDescSet_)
                .isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testValidate5() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.String[]");

        // ## Act ##
        boolean actual = DescValidator.validate(target, classDescSet_)
                .isValid();

        // ## Assert ##
        assertTrue(actual);
    }

    public void testValidate6() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.Hoehoe");

        // ## Act ##
        boolean actual = DescValidator.validate(target, classDescSet_)
                .isValid();

        // ## Assert ##
        assertFalse(actual);
    }
}
