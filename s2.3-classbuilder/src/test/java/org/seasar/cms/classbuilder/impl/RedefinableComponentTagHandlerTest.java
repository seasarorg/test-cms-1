package org.seasar.cms.classbuilder.impl;

import org.seasar.extension.unit.S2TestCase;


public class RedefinableComponentTagHandlerTest extends S2TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        include(getClass().getName().replace('.', '/').concat(".dicon"));
    }


    public void test_アスペクトが正しくかかること()
        throws Exception
    {
        assertEquals("intercept!", ((Hoe7Impl)getComponent(Hoe7Impl.class))
            .get());
    }
}
