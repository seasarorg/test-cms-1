package org.seasar.cms.classbuilder.impl;

import org.seasar.extension.unit.S2TestCase;


public class RedefinableXmlS2ContainerBuilderTest extends S2TestCase
{
    public void test_コンポーネントの再定義ができること()
        throws Exception
    {
        include("test1.dicon");
        Hoe hoe = (Hoe)getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }


    public void test_コンポーネントの追加ができること()
        throws Exception
    {
        include("test2.dicon");
        assertNotNull(getComponent(Hoe.class));
    }
}
