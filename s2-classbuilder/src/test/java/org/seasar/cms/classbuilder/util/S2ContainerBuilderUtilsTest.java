package org.seasar.cms.classbuilder.util;

import junit.framework.TestCase;


public class S2ContainerBuilderUtilsTest extends TestCase
{
    public void testFromURLToResourcePath()
        throws Exception
    {
        assertEquals("resource.txt", S2ContainerBuilderUtils
            .fromURLToResourcePath(getClass().getClassLoader().getResource(
                "resource.txt").toExternalForm()));
    }
}
