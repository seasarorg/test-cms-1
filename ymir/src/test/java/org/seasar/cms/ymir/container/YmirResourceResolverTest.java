package org.seasar.cms.ymir.container;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.util.ResourceUtil;

public class YmirResourceResolverTest extends TestCase {

    public void testCreateContainer1() throws Exception {

        // ## Arrange ##
        URL url = new File(ResourceUtil.getBuildDir(getClass()),
            "org/seasar/cms/ymir/container/test.dicon").toURI().toURL();

        // ## Act ##
        // ## Assert ##
        try {
            S2ContainerFactory.create(url.toExternalForm());
        } catch (Throwable t) {
            fail("URLで指定したdiconファイルからS2Containerを構築できること");
        }
    }

    public void testCreateContainer2() throws Exception {

        // ## Arrange ##
        URL url = new File(ResourceUtil.getBuildDir(getClass()),
            "org/seasar/cms/ymir/container/none.dicon").toURI().toURL();

        // ## Act ##
        // ## Assert ##
        try {
            S2ContainerFactory.create(url.toExternalForm());
            fail("URLで指定したdiconファイルが存在しない場合はS2Containerを構築できないこと");
        } catch (Throwable t) {
        }
    }

    public void testCreateContainer3() throws Exception {

        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
        try {
            S2ContainerFactory
                .create("org/seasar/cms/ymir/container/test.dicon");
        } catch (Throwable t) {
            fail("リソースパスで指定したdiconファイルからS2Containerを構築できること");
        }
    }

    public void testCreateContainer4() throws Exception {

        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
        URLClassLoader cl = new URLClassLoader(new URL[] { getClass()
            .getResource("test.jar") }, getClass().getClassLoader());
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);

            S2ContainerFactory.create("depends:test.jar");
        } catch (Throwable t) {
            fail("依存するJARが持つdiconファイルからS2Containerを構築できること");
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void testCreateContainer5() throws Exception {

        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
        URLClassLoader cl = new URLClassLoader(new URL[] { getClass()
            .getResource("test.jar") }, getClass().getClassLoader());
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);

            S2ContainerFactory.create("depends:test2.jar");
            fail("依存するJARが存在しない場合はS2Containerを構築できないこと");
        } catch (Throwable expected) {
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
