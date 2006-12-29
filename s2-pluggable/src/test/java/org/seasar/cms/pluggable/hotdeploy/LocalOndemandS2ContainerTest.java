package org.seasar.cms.pluggable.hotdeploy;

import junit.framework.TestCase;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;

public class LocalOndemandS2ContainerTest extends TestCase {

    public void test_同時アクセスしても正常に処理が行なわれること() throws Exception {
        SingletonPluggableContainerFactory.setConfigPath(getClass().getName()
                .replace('.', '/').concat("_app.dicon"));
        SingletonPluggableContainerFactory.prepareForContainer();
        SingletonPluggableContainerFactory.init();

        final S2Container rootContainer = SingletonPluggableContainerFactory
                .getInstance().getRootContainer();
        rootContainer.getExternalContext().setRequest(
                new MockHttpServletRequestImpl(new MockServletContextImpl("/"),
                        "/hoe.do"));

        final DistributedOndemandBehavior ondemand = (DistributedOndemandBehavior) S2ContainerBehavior
                .getProvider();
        Thread[] threads = new Thread[500];
        final int[] count = new int[1];
        final int[] okCount = new int[1];
        count[0] = threads.length;
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    ondemand.start();
                    for (int i = 0; i < 100; i++) {
                        new Object();
                    }
                    try {
                        if (rootContainer.hasComponentDef("hoeDto")) {
                            synchronized (okCount) {
                                okCount[0]++;
                            }
                        }
                    } finally {
                        ondemand.stop();
                        synchronized (count) {
                            count[0]--;
                        }
                    }
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        while (true) {
            synchronized (count) {
                if (count[0] == 0) {
                    break;
                }
            }
            Thread.sleep(1000);
        }
        assertEquals(threads.length, okCount[0]);
    }
}
