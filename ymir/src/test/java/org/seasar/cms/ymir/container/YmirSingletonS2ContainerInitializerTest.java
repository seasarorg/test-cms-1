package org.seasar.cms.ymir.container;

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.TooManyRegistrationRuntimeException;
import org.seasar.framework.container.factory.CircularIncludeRuntimeException;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.util.ResourceUtil;

public class YmirSingletonS2ContainerInitializerTest extends TestCase {

    private YmirSingletonS2ContainerInitializer target_;

    private URL[] resourceURLs_;

    private S2Container getChild(S2Container container, String path) {

        int size = container.getChildSize();
        for (int i = 0; i < size; i++) {
            if (path.equals(container.getChild(i).getPath())) {
                return container.getChild(i);
            }
        }
        return null;
    }

    protected void setUp() throws Exception {
        super.setUp();

        target_ = new YmirSingletonS2ContainerInitializer();
        resourceURLs_ = new URL[] {
            new File(ResourceUtil.getBuildDir(getClass()),
                "org/seasar/cms/ymir/container/listener1.dicon").toURI()
                .toURL(),
            new File(ResourceUtil.getBuildDir(getClass()),
                "org/seasar/cms/ymir/container/listener2.dicon").toURI()
                .toURL(), };
    }

    public void testInclude() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/root.dicon");

        // ## Act ##
        target_.include(container, resourceURLs_);

        // ## Assert ##
        assertTrue(getChild(container,
            "org/seasar/cms/ymir/container/included.dicon") == getChild(
            getChild(container, resourceURLs_[0].toExternalForm()),
            "org/seasar/cms/ymir/container/included.dicon"));

        // FIXME findComponents()の代わりにfindAllComponents()を使うようにしよう。
        Object[] listeners = container.findComponents(Listener.class);
        assertEquals(2, listeners.length);
        assertTrue(listeners[0] instanceof OneListener);
        assertTrue(listeners[1] instanceof TwoListener);
    }

    /*
     * S2Container#findComponents()はコンテナをまたがってコンポーネントを
     * 収集しないという仕様とのこと。
     */
    public void testSpike1() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/spike1.dicon");

        // ## Act ##
        Object[] listeners = container.findComponents(Listener.class);

        // ## Assert ##
        assertEquals(1, listeners.length);
        assertTrue(listeners[0] instanceof OneListener);
    }

    public void testSpike2() throws Exception {

        // ## Arrange ##

        // ## Act ##
        // ## Assert ##
        try {
            S2ContainerFactory
                .create("org/seasar/cms/ymir/container/spike2.dicon");
            fail("循環参照エラーになること");
        } catch (CircularIncludeRuntimeException expected) {
        }
    }

    public void testSpike3() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/spike3.dicon");

        // ## Act ##
        // ## Assert ##
        try {
            S2ContainerFactory.include(container,
                "org/seasar/cms/ymir/container/spike3.dicon");
            fail("循環参照エラーになること");
        } catch (CircularIncludeRuntimeException expected) {
        }
    }

    public void testSpike4() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/spike4.dicon");

        // ## Act ##
        // ## Assert ##
        try {
            container.getComponent(List.class);
        } catch (TooManyRegistrationRuntimeException ex) {
            fail("同じdiconを複数インクルードしていてもコンテナとしては1つとして扱われること");
        }
    }
}
