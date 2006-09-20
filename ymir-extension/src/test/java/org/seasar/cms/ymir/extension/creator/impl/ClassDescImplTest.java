package org.seasar.cms.ymir.extension.creator.impl;

import java.util.Map;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;

public class ClassDescImplTest extends SourceCreatorImplTestBase {

    public void testGetInstanceName() throws Exception {

        String actual = new ClassDescImpl("com.example.dto.TestDto")
                .getInstanceName();
        assertEquals("testDto", actual);
    }

    public void testMerge() throws Exception {

        ClassDesc actual = new ClassDescImpl("com.example.page.TestPage");
        PropertyDesc pd = new PropertyDescImpl("param1");
        pd.setMode(PropertyDesc.READ);
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param2");
        pd.setMode(PropertyDesc.WRITE);
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param3");
        pd.setTypeDesc("java.lang.String");
        pd.getTypeDesc().setExplicit(true);
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param4");
        pd.setTypeDesc("java.lang.String[]");
        actual.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param6");
        pd.setTypeDesc("java.lang.Integer");
        actual.setPropertyDesc(pd);
        MethodDesc md = new MethodDescImpl("method");
        actual.setMethodDesc(md);

        ClassDesc cd2 = new ClassDescImpl("com.example.page.TestPage");
        cd2.setSuperclassName("com.example.page.TestPageBase");
        pd = new PropertyDescImpl("param1");
        pd.setTypeDesc("java.lang.Integer");
        pd.getTypeDesc().setExplicit(true);
        pd.setMode(PropertyDesc.WRITE);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param2");
        pd.setTypeDesc("java.lang.Integer");
        pd.getTypeDesc().setExplicit(true);
        pd.setMode(PropertyDesc.READ);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param3");
        pd.setTypeDesc("java.lang.Integer");
        pd.getTypeDesc().setExplicit(true);
        cd2.setPropertyDesc(pd);
        pd = new PropertyDescImpl("param5");
        pd.setTypeDesc("java.lang.Integer[]");
        pd.getTypeDesc().setExplicit(true);
        cd2.setPropertyDesc(pd);
        md = new MethodDescImpl("method");
        md.setReturnTypeDesc("java.lang.Integer");
        md.getReturnTypeDesc().setExplicit(true);
        md.setBodyDesc(new BodyDescImpl("body"));
        cd2.setMethodDesc(md);

        actual.merge(cd2, true);

        assertEquals("com.example.page.TestPageBase", actual
                .getSuperclassName());
        assertEquals(6, actual.getPropertyDescs().length);
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
                .getPropertyDesc("param1").getMode());
        assertEquals("Integer", actual.getPropertyDesc("param1").getTypeDesc()
                .getName());
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
                .getPropertyDesc("param2").getMode());
        assertEquals("Integer", actual.getPropertyDesc("param2").getTypeDesc()
                .getName());
        assertEquals("String", actual.getPropertyDesc("param3").getTypeDesc()
                .getName());
        assertEquals(PropertyDesc.READ | PropertyDesc.WRITE, actual
                .getPropertyDesc("param1").getMode());
        assertTrue(actual.getPropertyDesc("param4").getTypeDesc().getName()
                .endsWith("[]"));
        assertEquals("Integer[]", actual.getPropertyDesc("param5")
                .getTypeDesc().getName());
        assertEquals("Integer", actual.getPropertyDesc("param6").getTypeDesc()
                .getName());
        MethodDesc actualMd = actual
                .getMethodDesc(new MethodDescImpl("method"));
        assertNotNull(actualMd);
        assertEquals("body", ((Map) actualMd.getBodyDesc().getRoot())
                .get("body"));
        assertEquals("Integer", actual.getMethodDesc(
                new MethodDescImpl("method")).getReturnTypeDesc().getName());
    }

    public void testMerge2() throws Exception {

        ClassDesc cd = target_.getClassDesc(
                "org.seasar.cms.ymir.extension.creator.impl.Merge3");
        ClassDesc cd2 = new ClassDescImpl(
                "org.seasar.cms.ymir.extension.creator.impl.Merge3");
        MethodDesc md = new MethodDescImpl("_render");
        md.setBodyDesc(new BodyDescImpl("//"));
        cd2.setMethodDesc(md);
        cd.merge(cd2, true);

        MethodDesc[] mds = cd.getMethodDescs();
        assertEquals(1, mds.length);
        assertNull("サブクラスが持っているメソッドはマージされないこと", mds[0].getBodyDesc());
    }
}
