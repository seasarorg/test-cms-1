package org.seasar.cms.classbuilder.util;

import junit.framework.TestCase;


public class S2ContainerBuilderUtilsTest extends TestCase
{
    public void testFromURLToResourcePath_ファイルリソースを指すURLからリソースパスを構築できること()
        throws Exception
    {
        assertEquals("resource.txt", S2ContainerBuilderUtils
            .fromURLToResourcePath(getClass().getClassLoader().getResource(
                "resource.txt").toExternalForm()));
    }


    public void testFromURLToResourcePath_プラス記号がパスに含まれる場合でも正しくリソースパスを構築できること()
        throws Exception
    {
        assertEquals("test/resource+.txt", S2ContainerBuilderUtils
            .fromURLToResourcePath(getClass().getClassLoader().getResource(
                "test/resource+.txt").toExternalForm()));
    }
}
