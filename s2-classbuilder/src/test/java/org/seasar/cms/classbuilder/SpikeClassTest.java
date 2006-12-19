package org.seasar.cms.classbuilder;

import java.util.HashMap;

import junit.framework.TestCase;


public class SpikeClassTest extends TestCase
{
    public void testGetMethod()
        throws Exception
    {
        try {
            Hoge.class.getMethod("getFuga", new Class[] { HashMap.class });
        } catch (NoSuchMethodException expected) {
        }
    }
}
