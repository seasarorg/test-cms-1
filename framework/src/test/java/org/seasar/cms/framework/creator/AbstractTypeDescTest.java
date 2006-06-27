package org.seasar.cms.framework.creator;

import junit.framework.TestCase;

public class AbstractTypeDescTest extends TestCase {

    private AbstractTypeDesc target_ = new AbstractTypeDesc() {
        public String getName() {
            return null;
        }
    };

    public void testGetInstanceName() throws Exception {

        String actual = target_.getInstanceName("ocm.example.dto.TestDto[]");
        assertEquals("testDtos", actual);
    }
}
