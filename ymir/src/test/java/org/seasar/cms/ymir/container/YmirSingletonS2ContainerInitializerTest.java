package org.seasar.cms.ymir.container;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.ResourceUtil;

public class YmirSingletonS2ContainerInitializerTest extends TestCase {

    private YmirSingletonS2ContainerInitializer target_;

    protected void setUp() throws Exception {
        super.setUp();

        target_ = new YmirSingletonS2ContainerInitializer() {
            URL[] getResourceURLs(String path) {
                try {
                    return new URL[] {
                        new File(ResourceUtil.getBuildDir(getClass()),
                            "org/seasar/cms/ymir/container/listener1.dicon")
                            .toURI().toURL(),
                        new File(ResourceUtil.getBuildDir(getClass()),
                            "org/seasar/cms/ymir/container/listener2.dicon")
                            .toURI().toURL(), };
                } catch (MalformedURLException ex) {
                    throw new IORuntimeException(ex);
                }
            }
        };
    }

    public void testGatherContainers() throws Exception {

        // ## Arrange ##

        // ## Act ##
        S2Container[] containers = target_.gatherContainers();

        // ## Assert ##
        assertEquals(2, containers.length);
        assertTrue(containers[0].hasComponentDef(OneListener.class));
        assertTrue(containers[1].hasComponentDef(TwoListener.class));
    }

    public void testIncludeContainer() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
            .create("org/seasar/cms/ymir/container/root.dicon");
        S2Container[] children = target_.gatherContainers();

        // ## Act ##
        target_.includeContainer(container, children);

        // ## Assert ##
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
}
