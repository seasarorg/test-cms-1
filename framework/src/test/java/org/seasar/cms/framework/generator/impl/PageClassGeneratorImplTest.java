package org.seasar.cms.framework.generator.impl;

import org.seasar.cms.framework.generator.ClassDesc;
import org.seasar.cms.framework.generator.PropertyDesc;

import junit.framework.TestCase;

public class PageClassGeneratorImplTest extends TestCase {

    public void testMergeClassDescs() throws Exception {

        ClassDesc cd1 = new ClassDesc("com.example.page.TestPage");
        PropertyDesc pd = new PropertyDesc("param1");
        pd.setMode(PropertyDesc.READ);
        cd1.setPropertyDesc(pd);
        pd = new PropertyDesc("param2");
        pd.setMode(PropertyDesc.WRITE);
        cd1.setPropertyDesc(pd);
        pd = new PropertyDesc("param3");
        pd.setType("java.lang.String");
        cd1.setPropertyDesc(pd);
        pd = new PropertyDesc("param4");
        pd.addMode(PropertyDesc.ARRAY);
        cd1.setPropertyDesc(pd);

        ClassDesc cd2 = new ClassDesc("com.example.page.TestPage");
        pd = new PropertyDesc("param1");
        pd.setType("java.lang.Integer");
        pd.setMode(PropertyDesc.WRITE);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDesc("param2");
        pd.setType("java.lang.Integer");
        pd.setMode(PropertyDesc.READ);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDesc("param3");
        pd.setType("java.lang.Integer");
        cd2.setPropertyDesc(pd);
        pd = new PropertyDesc("param5");
        pd.setType("java.lang.Integer");
        pd.addMode(PropertyDesc.ARRAY);
        cd2.setPropertyDesc(pd);

        ClassDesc actual = new PageClassGeneratorImpl().mergeClassDescs(cd1,
            cd2);

        assertEquals(5, actual.getPropertyDescs().length);
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
            .getPropertyDesc("param1").getMode());
        assertEquals("java.lang.Integer", actual.getPropertyDesc("param1")
            .getType());
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
            .getPropertyDesc("param2").getMode());
        assertEquals("java.lang.Integer", actual.getPropertyDesc("param2")
            .getType());
        assertEquals("java.lang.String", actual.getPropertyDesc("param3")
            .getType());
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
            .getPropertyDesc("param1").getMode());
        assertTrue(actual.getPropertyDesc("param4").isArray());
        assertEquals("java.lang.Integer", actual.getPropertyDesc("param5")
            .getType());
        assertTrue(actual.getPropertyDesc("param5").isArray());
    }
}
