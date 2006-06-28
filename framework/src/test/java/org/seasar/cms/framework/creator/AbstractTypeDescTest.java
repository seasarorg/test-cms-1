package org.seasar.cms.framework.creator;

import junit.framework.TestCase;

public class AbstractTypeDescTest extends TestCase {

    private AbstractTypeDesc target_ = new AbstractTypeDesc() {
        public String getName() {
            return null;
        }
    };

    public void testGetInstanceName() throws Exception {

        String actual = target_.getInstanceName("com.example.dto.TestDto[]");
        assertEquals("testDtos", actual);
    }

    public void testGetDefaultValue() throws Exception {

        String actual = target_.getDefaultValue("com.example.dto.TestDto[]");
        assertEquals("null", actual);

        actual = target_.getDefaultValue("byte");
        assertEquals("0", actual);

        actual = target_.getDefaultValue("short");
        assertEquals("0", actual);

        actual = target_.getDefaultValue("int");
        assertEquals("0", actual);

        actual = target_.getDefaultValue("long");
        assertEquals("0", actual);

        actual = target_.getDefaultValue("float");
        assertEquals("0", actual);

        actual = target_.getDefaultValue("double");
        assertEquals("0", actual);

        actual = target_.getDefaultValue("char");
        assertEquals("0", actual);

        actual = target_.getDefaultValue("boolean");
        assertEquals("false", actual);
    }
}
