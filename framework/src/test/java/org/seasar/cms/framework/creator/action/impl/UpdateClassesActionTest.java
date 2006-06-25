package org.seasar.cms.framework.creator.action.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.impl.SourceCreatorImplTestBase;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.kvasir.util.io.IOUtils;

public class UpdateClassesActionTest extends SourceCreatorImplTestBase {

    private UpdateClassesAction target_;

    protected void setUp() throws Exception {

        super.setUp();
        target_ = new UpdateClassesAction(getSourceCreator());
    }

    public void testGetClassDesc1() throws Exception {

        assertNull(target_.getClassDesc("hoge"));
    }

    public void testGetClassDesc2() throws Exception {

        ClassDesc actual = target_.getClassDesc("com.example.web.IndexPage");

        assertNotNull(actual);
        assertEquals("com.example.web.IndexPage", actual.getName());
        assertNull(actual.getSuperclassName());
        assertEquals(2, actual.getPropertyDescs().length);
        PropertyDesc pd = actual.getPropertyDesc("param1");
        assertNotNull(pd);
        assertEquals("java.lang.String", pd.getType());
        assertTrue(pd.isReadable());
        assertFalse(pd.isWritable());
        pd = actual.getPropertyDesc("param2");
        assertNotNull(pd);
        assertEquals("java.lang.Integer[]", pd.getType());
        assertFalse(pd.isReadable());
        assertTrue(pd.isWritable());
        MethodDesc md = actual.getMethodDesc("_render");
        assertNotNull(md);
        assertEquals("void", md.getReturnType());
    }

    public void testWriteSourceFile1() throws Exception {

        ClassDesc classDesc = constructClassDesc();
        File testPage = new File(ResourceUtil.getBuildDir(getClass()),
            classDesc.getName().replace('.', '/') + ".java");
        File testPageBase = new File(ResourceUtil.getBuildDir(getClass()),
            classDesc.getName().replace('.', '/') + "Base.java");

        testPage.delete();

        target_.writeSourceFile(classDesc);

        assertTrue(testPage.exists());
        assertTrue(testPageBase.exists());
    }

    public void testWriteSourceFile2() throws Exception {

        ClassDesc classDesc = constructClassDesc();
        File testPage = new File(ResourceUtil.getBuildDir(getClass()),
            classDesc.getName().replace('.', '/') + ".java");
        File testPageBase = new File(ResourceUtil.getBuildDir(getClass()),
            classDesc.getName().replace('.', '/') + "Base.java");

        testPage.getParentFile().mkdirs();
        testPageBase.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(testPage);
        os.write(32);
        os.close();
        os = new FileOutputStream(testPageBase);
        os.write(32);
        os.close();

        target_.writeSourceFile(classDesc);

        String actual = IOUtils.readString(new FileInputStream(testPage),
            "UTF-8", false);
        assertEquals(" ", actual);
        actual = IOUtils.readString(new FileInputStream(testPageBase), "UTF-8",
            false);
        assertFalse(" ".equals(actual));
    }

    public void testGatherClassDescs() throws Exception {

        File sourceDir = clean(new File(ResourceUtil.getBuildDir(getClass())
            .getParentFile(), "src"));
        getSourceCreator().setSourceDirectoryPath(sourceDir.getCanonicalPath());

        ClassDesc[] actual = target_.gatherClassDescs("/test.html",
            Request.METHOD_GET, "com.example.web.TestPage", getSourceCreator()
                .getTemplateFile("/test.html"));

        assertNotNull(actual);
        assertEquals(2, actual.length);
        assertEquals("com.example.web.TestPage", actual[0].getName());
        assertEquals("com.example.dto.EntityDto", actual[1].getName());
        MethodDesc md = actual[0].getMethodDesc("_get");
        assertNotNull(md);
        assertNull(md.getDefaultReturnType());
        assertNotNull(actual[0].getMethodDesc("_render"));
    }

    public void testShouldUpdate() throws Exception {

        File sourceDir = clean(new File(ResourceUtil.getBuildDir(getClass())
            .getParentFile(), "src"));
        getSourceCreator().setSourceDirectoryPath(sourceDir.getCanonicalPath());

        assertTrue(target_.shouldUpdate(getSourceCreator().getSourceFile(
            "com.example.web.TestPage"), getSourceCreator().getTemplateFile(
            "/test.html")));

        ClassDesc[] classDescs = target_.gatherClassDescs("/test.html",
            Request.METHOD_GET, "com.example.web.TestPage", getSourceCreator()
                .getTemplateFile("/test.html"));
        for (int i = 0; i < classDescs.length; i++) {
            target_.writeSourceFile(classDescs[i].merge(target_
                .getClassDesc(classDescs[i].getName())));
        }

        assertTrue(new File(sourceDir, "com/example/web/TestPage.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/web/TestPageBase.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/dto/EntityDto.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/dto/EntityDtoBase.java")
            .exists());

        assertFalse(target_.shouldUpdate(getSourceCreator().getSourceFile(
            "com.example.web.TestPage"), getSourceCreator().getTemplateFile(
            "/test.html")));
    }
}
