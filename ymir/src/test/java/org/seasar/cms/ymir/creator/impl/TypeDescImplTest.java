package org.seasar.cms.ymir.creator.impl;

import junit.framework.TestCase;

public class TypeDescImplTest extends TestCase {

    public void testGetDefaultValue() throws Exception {

        String actual = new TypeDescImpl("com.example.dto.TestDto[]")
            .getDefaultValue();
        assertEquals("null", actual);

        actual = new TypeDescImpl("byte").getDefaultValue();
        assertEquals("0", actual);

        actual = new TypeDescImpl("short").getDefaultValue();
        assertEquals("0", actual);

        actual = new TypeDescImpl("int").getDefaultValue();
        assertEquals("0", actual);

        actual = new TypeDescImpl("long").getDefaultValue();
        assertEquals("0", actual);

        actual = new TypeDescImpl("float").getDefaultValue();
        assertEquals("0", actual);

        actual = new TypeDescImpl("double").getDefaultValue();
        assertEquals("0", actual);

        actual = new TypeDescImpl("char").getDefaultValue();
        assertEquals("0", actual);

        actual = new TypeDescImpl("boolean").getDefaultValue();
        assertEquals("false", actual);
    }
}
