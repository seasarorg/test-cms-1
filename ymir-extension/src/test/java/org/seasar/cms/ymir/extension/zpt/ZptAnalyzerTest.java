package org.seasar.cms.ymir.extension.zpt;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.impl.MethodDescImpl;
import org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl;
import org.seasar.cms.ymir.impl.ApplicationManagerImpl;
import org.seasar.cms.ymir.mock.MockApplication;
import org.seasar.framework.convention.impl.NamingConventionImpl;

public class ZptAnalyzerTest extends TestCase {

    private static final String CLASSNAME = "com.example.web.IndexPage";

    private ZptAnalyzer analyzer_ = new ZptAnalyzer();

    private Map<String, ClassDesc> classDescMap_ = new HashMap<String, ClassDesc>();

    protected void setUp() throws Exception {

        analyzer_ = new ZptAnalyzer() {
            @Override
            boolean isUsingFreyjaRenderClasses() {
                return false;
            }
        };
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

            @Override
            public String getRootPackageName() {
                return "com.example";
            }
        };
        creator.setNamingConvention(new NamingConventionImpl());
        ApplicationManagerImpl applicationManager = new ApplicationManagerImpl();
        applicationManager.setBaseApplication(new MockApplication() {
            public Enumeration propertyNames() {
                return new Vector().elements();
            }
        });
        creator.setApplicationManager(applicationManager);
        analyzer_.setSourceCreator(creator);
    }

    private void act(String methodName) {

        analyzer_.analyze("/hoe", Request.METHOD_GET, classDescMap_, getClass()
                .getResourceAsStream("ZptAnalyzerTest_" + methodName + ".zpt"),
                "UTF-8", CLASSNAME);
    }

    private ClassDesc getClassDesc(String name) {

        return (ClassDesc) classDescMap_.get(name);
    }

    public void testAnalyze1() throws Exception {

        act("testAnalyze1");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        assertNotNull(cd.getPropertyDesc("body"));
        assertNull("TemplateAnalyzerではリクエストメソッドのためのメソッド定義を生成しないこと", cd
                .getMethodDesc(new MethodDescImpl("GET")));
    }

    public void testAnalyze2() throws Exception {

        act("testAnalyze2");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("text");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        ClassDesc cd2 = getClassDesc("com.example.web.ActionPage");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("text");
        assertNotNull(pd2);
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze3() throws Exception {

        act("testAnalyze3");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entities");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertFalse(pd.getTypeDesc().isExplicit());
        assertEquals("com.example.dto.EntityDto[]", pd.getTypeDesc().getName());
        ClassDesc cd2 = getClassDesc("com.example.dto.EntityDto");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("content");
        assertNotNull(pd2);
        assertTrue(pd2.isReadable());
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze4() throws Exception {

        act("testAnalyze4");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("strings");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertFalse(pd.getTypeDesc().isExplicit());
        assertEquals("プロパティを持たないリピート対象変数はStringの配列になること", "String[]", pd
                .getTypeDesc().getName());

        pd = cd.getPropertyDesc("entities");
        assertEquals("プロパティを持つリピート対象変数はDtoの配列になること",
                "com.example.dto.EntityDto[]", pd.getTypeDesc().getName());
        cd = getClassDesc("com.example.dto.EntityDto");
        assertNotNull("プロパティを持つリピート対象変数の型が生成されていること", cd);
        assertNotNull("Dto型がプロパティを持つこと", cd.getPropertyDesc("content"));
    }

    public void testAnalyze5() throws Exception {

        act("testAnalyze5");

        ClassDesc cd = getClassDesc("com.example.web.UpdatePage");
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
        assertEquals(FormFile.class.getName(), pd.getTypeDesc().getName());
        assertNull(cd.getPropertyDesc("button"));
        assertNull(cd.getPropertyDesc("image"));
        assertNull(cd.getPropertyDesc("submit"));
        MethodDesc md = cd.getMethodDesc(new MethodDescImpl("POST"));
        assertNotNull(md);
        assertEquals("void", md.getReturnTypeDesc().getName());
    }

    public void testAnalyze6() throws Exception {

        act("testAnalyze6");

        ClassDesc cd = getClassDesc("com.example.web.TestPage");
        assertNotNull("コンポーネント名が指定されている場合は対応するクラスのためのClassDescを生成すること", cd);
        assertNotNull(cd.getPropertyDesc("body"));
    }

    public void testAnalyze7() throws Exception {

        act("testAnalyze7");

        ClassDesc actual = getClassDesc("com.example.web.ActionPage");
        assertNotNull(actual);
        MethodDesc md = actual.getMethodDesc(new MethodDescImpl("GET"));
        assertNotNull(md);
        PropertyDesc pd = actual.getPropertyDesc("id");
        assertNotNull(pd);
    }

    public void testAnalyze8() throws Exception {

        act("testAnalyze8");

        ClassDesc actual = getClassDesc("com.example.web.ActionPage");
        assertNotNull(actual);
        MethodDesc md = actual.getMethodDesc(new MethodDescImpl("GET"));
        assertNotNull(md);
        PropertyDesc pd = actual.getPropertyDesc("id");
        assertNotNull(pd);
    }

    public void testAnalyze9() throws Exception {

        act("testAnalyze9");

        ClassDesc cd = getClassDesc("com.example.web.ActionPage");
        PropertyDesc pd = cd.getPropertyDesc("test");
        assertNotNull("formのパラメータに対応するプロパティが生成されること", pd);
        assertEquals("com.example.dto.TestDto", pd.getTypeDesc().getName());
        assertTrue(pd.isReadable());

        cd = getClassDesc("com.example.dto.TestDto");
        assertNotNull("formのパラメータを保持するためのDtoクラスが生成されること", cd);
        assertNotNull(cd.getPropertyDesc("name"));
        assertNotNull(cd.getPropertyDesc("body"));
    }

    public void testAnalyze10() throws Exception {

        act("testAnalyze10");

        ClassDesc cd = getClassDesc("com.example.dto.TestDto");
        PropertyDesc pd = cd.getPropertyDesc("file");
        assertEquals("ファイルパラメータについてはプロパティの型がFormFileになること", FormFile.class
                .getName(), pd.getTypeDesc().getName());
    }

    public void testAnalyze11() throws Exception {

        act("testAnalyze11");

        ClassDesc cd = getClassDesc("com.example.web.ActionPage");
        PropertyDesc pd = cd.getPropertyDesc("tests");
        assertTrue("フォームパラメータ名に添字指定がある場合は配列になること", pd.getTypeDesc().isArray());
        assertEquals("tests配列プロパティのDto型名は単数形になること", "com.example.dto.TestDto",
                pd.getTypeDesc().getClassDesc().getName());
    }

    public void testAnalyze12() throws Exception {

        act("testAnalyze12");

        ClassDesc cd = getClassDesc("com.example.dto.TestDto");
        PropertyDesc pd = cd.getPropertyDesc("files");
        assertEquals("添字指定があるファイルパラメータについてはプロパティの型がFormFileの配列になること",
                FormFile.class.getName() + "[]", pd.getTypeDesc().getName());
    }

    public void testAnalyze13() throws Exception {

        act("testAnalyze13");

        ClassDesc cd = getClassDesc("com.example.web.Test0Page");
        assertNotNull("page:指定のパラメータ置換が正しく行われ、その結果Pageクラスが生成されること", cd);
    }

    public void testAnalyze14() throws Exception {

        act("testAnalyze14");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entity");
        assertEquals("プロパティを持つ変数はDtoの配列になること", "com.example.dto.EntityDto", pd
                .getTypeDesc().getName());
        cd = getClassDesc("com.example.dto.EntityDto");
        assertNotNull("プロパティを持つ変数の型が生成されていること", cd);
        assertNotNull("Dto型がプロパティを持つこと", cd.getPropertyDesc("content"));
    }
}
