package org.seasar.cms.framework.creator;

import junit.framework.TestCase;

public class ClassDescTest extends TestCase {

    public void testMerge() throws Exception {

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
        pd.setDefaultType("java.lang.String[]");
        cd1.setPropertyDesc(pd);
        pd = new PropertyDesc("param6");
        pd.setDefaultType("java.lang.Integer");
        cd1.setPropertyDesc(pd);
        MethodDesc md = new MethodDesc("method");
        cd1.setMethodDesc(md);

        ClassDesc cd2 = new ClassDesc("com.example.page.TestPage");
        cd2.setSuperclassName("com.example.page.TestPageBase");
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
        pd.setType("java.lang.Integer[]");
        cd2.setPropertyDesc(pd);
        md = new MethodDesc("method");
        md.setReturnType("java.lang.Integer");
        md.setBody("body");
        cd2.setMethodDesc(md);

        ClassDesc actual = cd1.merge(cd2, true);

        assertEquals("com.example.page.TestPageBase", actual
            .getSuperclassName());
        assertEquals(6, actual.getPropertyDescs().length);
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
        assertTrue(actual.getPropertyDesc("param4").getTypeString().endsWith(
            "[]"));
        assertEquals("java.lang.Integer[]", actual.getPropertyDesc("param5")
            .getType());
        assertEquals("Integer", actual.getPropertyDesc("param6")
            .getTypeString());
        MethodDesc actualMd = actual.getMethodDesc("method");
        assertNotNull(actualMd);
        assertEquals("body", actualMd.getBody());
        assertEquals("java.lang.Integer", actual.getMethodDesc("method")
            .getReturnType());
    }
}
