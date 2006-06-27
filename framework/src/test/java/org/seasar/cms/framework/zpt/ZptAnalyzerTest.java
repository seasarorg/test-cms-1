package org.seasar.cms.framework.zpt;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.seasar.cms.framework.FormFile;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;

public class ZptAnalyzerTest extends TestCase {

    private static final String CLASSNAME = "com.example.web.IndexPage";

    private ZptAnalyzer analyzer_ = new ZptAnalyzer();

    private Map classDescriptorMap_ = new HashMap();

    protected void setUp() throws Exception {

        analyzer_ = new ZptAnalyzer();
        SourceCreatorImpl creator = new SourceCreatorImpl() {
            public String getComponentName(String path, String method) {
                int slash = path.lastIndexOf('/');
                if (slash >= 0) {
                    path = path.substring(slash + 1);
                }
                int dot = path.lastIndexOf('.');
                if (dot >= 0) {
                    path = path.substring(0, dot);
                }
                return path + "Page";
            }

            public String getClassName(String componentName) {
                if (componentName.endsWith("Page")) {
                    return "com.example.web."
                        + Character.toUpperCase(componentName.charAt(0))
                        + componentName.substring(1);
                } else {
                    return null;
                }
            }

            public String getActionName(String path, String method) {
                return method;
            }
        };
        creator.setPagePackageName("com.example.web");
        creator.setDtoPackageName("com.example.dto");
        analyzer_.setSourceCreator(creator);
    }

    private void act(String methodName) {

        analyzer_.analyze(Request.METHOD_GET, classDescriptorMap_, getClass()
            .getResourceAsStream("ZptAnalyzerTest_" + methodName + ".zpt"),
            "UTF-8", CLASSNAME);
    }

    private ClassDesc getClassDescriptor(String name) {

        return (ClassDesc) classDescriptorMap_.get(name);
    }

    public void testAnalyze1() throws Exception {

        act("testAnalyze1");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        assertNotNull(cd.getPropertyDesc("body"));
        assertNull("TemplateAnalyzerではリクエストメソッドのためのメソッド定義を生成しないこと", cd
            .getMethodDesc(new MethodDesc("GET")));
    }

    public void testAnalyze2() throws Exception {

        act("testAnalyze2");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("text");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        ClassDesc cd2 = getClassDescriptor("com.example.web.ActionPage");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("text");
        assertNotNull(pd2);
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze3() throws Exception {

        act("testAnalyze3");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entities");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertNull(pd.getTypeDesc().getType());
        assertEquals("com.example.dto.EntityDto[]", pd.getTypeDesc()
            .getDefaultType());
        ClassDesc cd2 = getClassDescriptor("com.example.dto.EntityDto");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("content");
        assertNotNull(pd2);
        assertTrue(pd2.isReadable());
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze4() throws Exception {

        act("testAnalyze4");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entities");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertNull(pd.getTypeDesc().getType());
        assertEquals("java.lang.String[]", pd.getTypeDesc().getDefaultType());
    }

    public void testAnalyze5() throws Exception {

        act("testAnalyze5");

        ClassDesc cd = getClassDescriptor("com.example.web.UpdatePage");
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("text");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isWritable());
        pd = cd.getPropertyDesc("select");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isWritable());
        pd = cd.getPropertyDesc("textarea");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isWritable());
        pd = cd.getPropertyDesc("file");
        assertNotNull(pd);
        assertEquals(FormFile.class.getName(), pd.getTypeDesc()
            .getDefaultType());
        assertNull(cd.getPropertyDesc("button"));
        assertNull(cd.getPropertyDesc("image"));
        assertNull(cd.getPropertyDesc("submit"));
        MethodDesc md = cd.getMethodDesc(new MethodDesc("POST"));
        assertNotNull(md);
        assertEquals("void", md.getReturnTypeDesc().getName());
    }

    public void testAnalyze6() throws Exception {

        act("testAnalyze6");

        ClassDesc cd = getClassDescriptor("com.example.web.TestPage");
        assertNotNull("コンポーネント名が指定されている場合は対応するクラスのためのClassDescを生成すること", cd);
        assertNotNull(cd.getPropertyDesc("body"));
    }

    public void testAnalyze7() throws Exception {

        act("testAnalyze7");

        ClassDesc actual = getClassDescriptor("com.example.web.ActionPage");
        assertNotNull(actual);
        MethodDesc md = actual.getMethodDesc(new MethodDesc("GET"));
        assertNotNull(md);
        PropertyDesc pd = actual.getPropertyDesc("id");
        assertNotNull(pd);
    }

    public void testAnalyze8() throws Exception {

        act("testAnalyze8");

        ClassDesc actual = getClassDescriptor("com.example.web.ActionPage");
        assertNotNull(actual);
        MethodDesc md = actual.getMethodDesc(new MethodDesc("GET"));
        assertNotNull(md);
        PropertyDesc pd = actual.getPropertyDesc("id");
        assertNotNull(pd);
    }
}
