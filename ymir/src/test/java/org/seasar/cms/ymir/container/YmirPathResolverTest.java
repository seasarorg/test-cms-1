package org.seasar.cms.ymir.container;

import java.net.URL;
import java.net.URLClassLoader;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

import junit.framework.TestCase;

public class YmirPathResolverTest extends TestCase {

    public void testGetResourceURL() throws Exception {

        // ## Arrange ##
        URL[] urls = new URL[] {
            new URL("jar:file:/path/to/jar/test-0.0.1.jar!/"),
            new URL("jar:file:/path/to/jar/hoge-0.0.1.jar!/"), };

        // ## Act ##
        URL actual = new YmirPathResolver().getResourceURL("test", urls);

        // ## Assert ##
        assertEquals(urls[0], actual);

        // ## Act ##
        actual = new YmirPathResolver().getResourceURL("hoe", urls);

        // ## Assert ##
        assertNull(actual);
    }

    public void testDepends指定したものと直接指定したものが同じコンテナを指すこと() throws Exception {

        // ## Arrange ##
        // ## Act ##
        S2Container container1;
        S2Container container2;
        URL testJarURL = getClass().getResource("test.jar");
        URLClassLoader cl = new URLClassLoader(new URL[] { testJarURL },
            getClass().getClassLoader());
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            S2Container rootContainer = S2ContainerFactory.create(getClass()
                .getName().replace('.', '/')
                + "_root.dicon");
            container1 = rootContainer.getChild(0);
            container2 = S2ContainerFactory.include(rootContainer, "jar:"
                + testJarURL.toExternalForm()
                + "!/META-INF/s2container/components.dicon");
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        // ## Assert ##
        assertTrue(container1 == container2);
    }
}
