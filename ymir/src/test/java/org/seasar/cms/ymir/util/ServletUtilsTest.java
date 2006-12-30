package org.seasar.cms.ymir.util;

import junit.framework.TestCase;

public class ServletUtilsTest extends TestCase {

    public void testStripParametersAndQueries1() throws Exception {

        assertNull(ServletUtils.stripParametersAndQueries(null));
    }

    public void testStripParametersAndQueries2() throws Exception {

        assertEquals("/path/to/what", ServletUtils
                .stripParametersAndQueries("/path/to/what"));
    }

    public void testStripParametersAndQueries3() throws Exception {

        assertEquals("/path/to/what", ServletUtils
                .stripParametersAndQueries("/path/to/what;jsessionid=HOEHOE"));

    }

    public void testStripParametersAndQueries4() throws Exception {
        assertEquals(
                "/path/to/what",
                ServletUtils
                        .stripParametersAndQueries("/path/to/what;jsessionid=HOEHOE?fuga=hehe"));
    }

    public void testStripParametersAndQueries5() throws Exception {

        assertEquals("/path/to/what", ServletUtils
                .stripParametersAndQueries("/path/to/what?fuga=hehe"));
    }
}
