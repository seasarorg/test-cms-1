package org.seasar.cms.framework.generator.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import org.seasar.cms.framework.FrameworkTestCase;
import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.container.hotdeploy.DistributedOndemandBehavoir;
import org.seasar.cms.framework.container.hotdeploy.LocalOndemandCreatorContainer;
import org.seasar.cms.framework.container.hotdeploy.OndemandUtils;
import org.seasar.cms.framework.freemarker.FreemarkerSourceGenerator;
import org.seasar.cms.framework.generator.ClassDesc;
import org.seasar.cms.framework.generator.PropertyDesc;
import org.seasar.cms.framework.generator.SourceCreator;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;
import org.seasar.cms.framework.zpt.ZptAnalyzer;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.HttpServletComponentDeployerProvider;
import org.seasar.framework.container.hotdeploy.OndemandCreatorContainer;
import org.seasar.framework.container.hotdeploy.creator.PageCreator;
import org.seasar.framework.container.impl.HttpServletExternalContext;
import org.seasar.framework.container.impl.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.container.impl.S2ContainerImpl;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.kvasir.util.io.IOUtils;

public class SourceCreatorImplTest extends FrameworkTestCase {

    private S2Container container_;

    private SourceCreatorImpl target_;

    private ClassDesc constructClassDesc() {
        ClassDesc classDesc = new ClassDesc("com.example.web.TestPage");
        PropertyDesc pd = new PropertyDesc("param1");
        pd.setType("java.lang.String");
        pd.setMode(PropertyDesc.READ | PropertyDesc.WRITE);
        classDesc.setPropertyDesc(pd);
        return classDesc;
    }

    protected void setUp() throws Exception {

        ComponentDeployerFactory
            .setProvider(new HttpServletComponentDeployerProvider());
        S2ContainerBehavior.setProvider(new DistributedOndemandBehavoir());
        container_ = new S2ContainerImpl();
        ((S2ContainerImpl) container_).setClassLoader(getClass()
            .getClassLoader());
        container_.setExternalContext(new HttpServletExternalContext());
        container_
            .setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
        ServletContext context = new MockServletContextImpl("/context");
        MockHttpServletRequestImpl request = new MockHttpServletRequestImpl(
            context, "/servlet");
        container_.getExternalContext().setRequest(request);
        container_.getExternalContext().setResponse(
            new MockHttpServletResponseImpl(request));
        container_.register(SourceCreatorImpl.class);
        container_.register(DefaultRequestProcessor.class);
        container_.register(LocalOndemandCreatorContainer.class);
        container_.register(ZptAnalyzer.class);

        DefaultRequestProcessor processor = (DefaultRequestProcessor) container_
            .getComponent(RequestProcessor.class);
        processor.addMapping("^/([^/]+)\\.(.+)$", "${1}Page", "_${method}", "");
        LocalOndemandCreatorContainer creatorContainer = (LocalOndemandCreatorContainer) container_
            .getComponent(OndemandCreatorContainer.class);
        creatorContainer.setRootPackageName("com.example");
        creatorContainer.addCreator(new PageCreator());
        OndemandUtils.start(container_);

        target_ = (SourceCreatorImpl) container_
            .getComponent(SourceCreator.class);
        target_.setDtoPackageName("com.example.dto");
        target_.setSourceDirectoryPath(ResourceUtil.getBuildDir(getClass())
            .getCanonicalPath());
        target_.setClassesDirectoryPath(ResourceUtil.getBuildDir(getClass())
            .getCanonicalPath());
        target_.setWebappDirectoryPath(new File(ResourceUtil
            .getBuildDir(getClass()), "webapp").getCanonicalPath());
        target_.setSourceGenerator(new FreemarkerSourceGenerator());
    }

    protected void tearDown() throws Exception {

        OndemandUtils.stop(container_);
    }

    public void testGetComponentName() throws Exception {

        String actual = target_.getComponentName("/index.html");

        assertEquals("indexPage", actual);
    }

    public void testGetClassName1() throws Exception {

        assertNull(target_.getClassName(null));
    }

    public void testGetClassName2() throws Exception {

        String actual = target_.getClassName("indexPage");

        assertEquals("com.example.web.IndexPage", actual);
    }

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
        pd = new PropertyDesc("param6");
        pd.setDefaultType("java.lang.Integer");
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

        ClassDesc actual = new SourceCreatorImpl().mergeClassDescs(cd1, cd2);

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
        assertTrue(actual.getPropertyDesc("param4").isArray());
        assertEquals("java.lang.Integer", actual.getPropertyDesc("param5")
            .getType());
        assertTrue(actual.getPropertyDesc("param5").isArray());
        assertEquals("Integer", actual.getPropertyDesc("param6")
            .getTypeString());
    }

    public void testGetClassDesc1() throws Exception {

        assertNull(target_.getClassDesc("hoge"));
    }

    public void testGetClassDesc2() throws Exception {

        ClassDesc actual = target_.getClassDesc("com.example.web.IndexPage");

        assertNotNull(actual);
        assertEquals("com.example.web.IndexPage", actual.getName());
        assertEquals(2, actual.getPropertyDescs().length);
        PropertyDesc pd = actual.getPropertyDesc("param1");
        assertNotNull(pd);
        assertEquals("java.lang.String", pd.getType());
        assertFalse(pd.isArray());
        assertTrue(pd.isReadable());
        assertFalse(pd.isWritable());
        pd = actual.getPropertyDesc("param2");
        assertNotNull(pd);
        assertEquals("java.lang.Integer", pd.getType());
        assertTrue(pd.isArray());
        assertFalse(pd.isReadable());
        assertTrue(pd.isWritable());
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

    public void testUpdate() throws Exception {

        File sourceDir = clean(new File(ResourceUtil.getBuildDir(getClass())
            .getParentFile(), "src"));
        target_.setSourceDirectoryPath(sourceDir.getCanonicalPath());

        ClassDesc[] actual = target_.update("/test.html");

        assertNotNull(actual);
        assertEquals(2, actual.length);
        assertEquals("com.example.web.TestPage", actual[0].getName());
        assertEquals("com.example.dto.EntityDto", actual[1].getName());
        assertTrue(new File(sourceDir, "com/example/web/TestPage.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/web/TestPageBase.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/dto/EntityDto.java").exists());
        assertTrue(new File(sourceDir, "com/example/dto/EntityDtoBase.java")
            .exists());

        actual = target_.update("/test.html");
        assertNull(actual);
    }
}
