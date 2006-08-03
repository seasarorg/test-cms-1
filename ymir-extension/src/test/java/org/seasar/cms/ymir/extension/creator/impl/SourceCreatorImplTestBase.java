package org.seasar.cms.ymir.extension.creator.impl;

import java.io.File;

import javax.servlet.ServletContext;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.cms.pluggable.hotdeploy.DistributedOndemandBehavior;
import org.seasar.cms.pluggable.hotdeploy.LocalOndemandS2Container;
import org.seasar.cms.pluggable.hotdeploy.TemporaryLocalOndemandS2Container;
import org.seasar.cms.pluggable.impl.ConfigurationImpl;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.YmirTestCase;
import org.seasar.cms.ymir.convention.YmirNamingConvention;
import org.seasar.cms.ymir.extension.Globals;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.freemarker.FreemarkerSourceGenerator;
import org.seasar.cms.ymir.extension.zpt.ZptAnalyzer;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.external.servlet.HttpServletExternalContext;
import org.seasar.framework.container.external.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.container.hotdeploy.OndemandCreator;
import org.seasar.framework.container.hotdeploy.OndemandProject;
import org.seasar.framework.container.hotdeploy.creator.PageOndemandCreator;
import org.seasar.framework.container.hotdeploy.impl.OndemandProjectImpl;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.container.impl.S2ContainerImpl;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;
import org.seasar.framework.util.ResourceUtil;

abstract public class SourceCreatorImplTestBase extends YmirTestCase {

    protected S2Container container_;

    protected DistributedOndemandBehavior ondemandBehavior_;

    protected SourceCreatorImpl target_;

    protected ClassDesc constructClassDesc() {
        ClassDesc classDesc = new ClassDescImpl("com.example.web.TestPage");
        PropertyDesc pd = new PropertyDescImpl("param1");
        pd.setMode(PropertyDesc.READ | PropertyDesc.WRITE);
        classDesc.setPropertyDesc(pd);
        return classDesc;
    }

    protected SourceCreatorImpl getSourceCreator() {

        return target_;
    }

    protected void setUp() throws Exception {

        SingletonPluggableContainerFactory.prepareForContainer();

        container_ = new S2ContainerImpl();
        container_.setClassLoader(getClass().getClassLoader());
        SingletonPluggableContainerFactory.getRootContainer().include(
                container_);
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
        container_.register(TemporaryLocalOndemandS2Container.class);
        container_.register(OndemandProjectImpl.class);
        container_.register(YmirNamingConvention.class);
        container_.register(ZptAnalyzer.class);
        container_.register(ConfigurationImpl.class);

        ondemandBehavior_ = (DistributedOndemandBehavior) S2ContainerBehavior
                .getProvider();

        OndemandProjectImpl project = (OndemandProjectImpl) container_
                .getComponent(OndemandProject.class);
        project.setRootPackageName("com.example");
        NamingConvention namingConvention = (NamingConvention) container_
                .getComponent(NamingConvention.class);
        project.setCreators(new OndemandCreator[] { new PageOndemandCreator(
                namingConvention) });
        LocalOndemandS2Container ondemandContainer = (LocalOndemandS2Container) container_
                .getComponent(LocalOndemandS2Container.class);
        ondemandContainer.addProject(project);

        Configuration configuration = (Configuration) container_
                .getComponent(Configuration.class);
        configuration.setProperty(Globals.KEY_WEBAPPROOT, new File(ResourceUtil
                .getBuildDir(getClass()), "webapp").getCanonicalPath());

        DefaultRequestProcessor processor = (DefaultRequestProcessor) container_
                .getComponent(RequestProcessor.class);
        processor.addPathMapping("^/([^/]+)\\.(.+)$", "${1}Page", "_${method}",
                "", null);

        target_ = (SourceCreatorImpl) container_
                .getComponent(SourceCreator.class);
        target_.setNamingConvention(namingConvention);
        target_.setSourceDirectoryPath(ResourceUtil.getBuildDir(getClass())
                .getCanonicalPath());
        target_.setClassesDirectoryPath(ResourceUtil.getBuildDir(getClass())
                .getCanonicalPath());
        target_.setResourcesDirectoryPath(ResourceUtil.getBuildDir(getClass())
                .getCanonicalPath());
        FreemarkerSourceGenerator sourceGenerator = new FreemarkerSourceGenerator();
        sourceGenerator.setSourceCreator(target_);
        target_.setSourceGenerator(sourceGenerator);

        SingletonPluggableContainerFactory.init();
    }

    protected void tearDown() throws Exception {

        SingletonPluggableContainerFactory.destroy();
    }
}
