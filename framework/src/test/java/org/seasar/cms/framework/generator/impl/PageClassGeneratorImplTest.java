package org.seasar.cms.framework.generator.impl;

import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.container.hotdeploy.DistributedOndemandBehavoir;
import org.seasar.cms.framework.container.hotdeploy.LocalOndemandCreatorContainer;
import org.seasar.cms.framework.container.hotdeploy.OndemandUtils;
import org.seasar.cms.framework.generator.ClassDesc;
import org.seasar.cms.framework.generator.PageClassGenerator;
import org.seasar.cms.framework.generator.PropertyDesc;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;
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

public class PageClassGeneratorImplTest extends TestCase {

    private S2Container container_;

    private PageClassGeneratorImpl target_;

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
        container_.register(PageClassGeneratorImpl.class);
        container_.register(DefaultRequestProcessor.class);
        container_.register(LocalOndemandCreatorContainer.class);

        DefaultRequestProcessor processor = (DefaultRequestProcessor) container_
            .getComponent(RequestProcessor.class);
        processor.addMapping("^/([^/]+)\\.(.+)$", "${1}Page", "_${method}", "");
        LocalOndemandCreatorContainer creatorContainer = (LocalOndemandCreatorContainer) container_
            .getComponent(OndemandCreatorContainer.class);
        creatorContainer.setRootPackageName("com.example");
        creatorContainer.addCreator(new PageCreator());
        OndemandUtils.start(container_);

        target_ = (PageClassGeneratorImpl) container_
            .getComponent(PageClassGenerator.class);
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

        ClassDesc actual = new PageClassGeneratorImpl().mergeClassDescs(cd1,
            cd2);

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
}
