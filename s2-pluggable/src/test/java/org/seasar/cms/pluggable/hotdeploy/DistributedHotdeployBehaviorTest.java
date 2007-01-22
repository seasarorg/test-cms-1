package org.seasar.cms.pluggable.hotdeploy;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;

import junit.framework.TestCase;

public class DistributedHotdeployBehaviorTest extends TestCase {
    protected void tearDown() throws Exception {
        SingletonPluggableContainerFactory.destroy();
    }

    public void test_開発モードではLocalHotdeployS2Containerのモードに従ってhotdeployやcooldeployになること()
            throws Exception {
        SingletonPluggableContainerFactory.setConfigPath(getClass().getName()
                .replace('.', '/').concat("_app.dicon"));
        SingletonPluggableContainerFactory.prepareForContainer();
        SingletonPluggableContainerFactory.init();

        S2Container rootContainer = SingletonPluggableContainerFactory
                .getInstance().getRootContainer();
        rootContainer.getExternalContext().setRequest(
                new MockHttpServletRequestImpl(new MockServletContextImpl("/"),
                        "/hoe.do"));

        DistributedHotdeployBehavior ondemand = (DistributedHotdeployBehavior) S2ContainerBehavior
                .getProvider();

        ComponentDef expected;
        ondemand.start();
        try {
            expected = rootContainer.getComponentDef("hotDto");
        } finally {
            ondemand.stop();
        }

        ComponentDef actual;
        ondemand.start();
        try {
            actual = rootContainer.getComponentDef("hotDto");
        } finally {
            ondemand.stop();
        }

        assertNotSame(expected.getComponentClass(), actual.getComponentClass());

        ondemand.start();
        try {
            expected = rootContainer.getComponentDef("coolDto");
        } finally {
            ondemand.stop();
        }

        ondemand.start();
        try {
            actual = rootContainer.getComponentDef("coolDto");
        } finally {
            ondemand.stop();
        }

        assertSame(expected.getComponentClass(), actual.getComponentClass());
    }

    public void test_本番モードではLocalHotdeployS2Containerのモードとは無関係にcooldeployになること()
            throws Exception {
        SingletonPluggableContainerFactory.setConfigPath(getClass().getName()
                .replace('.', '/').concat("_app2.dicon"));
        SingletonPluggableContainerFactory.prepareForContainer();
        SingletonPluggableContainerFactory.init();

        S2Container rootContainer = SingletonPluggableContainerFactory
                .getInstance().getRootContainer();
        rootContainer.getExternalContext().setRequest(
                new MockHttpServletRequestImpl(new MockServletContextImpl("/"),
                        "/hoe.do"));

        DistributedHotdeployBehavior ondemand = (DistributedHotdeployBehavior) S2ContainerBehavior
                .getProvider();

        ComponentDef expected;
        ondemand.start();
        try {
            expected = rootContainer.getComponentDef("hotDto");
        } finally {
            ondemand.stop();
        }

        ComponentDef actual;
        ondemand.start();
        try {
            actual = rootContainer.getComponentDef("hotDto");
        } finally {
            ondemand.stop();
        }

        assertSame(expected.getComponentClass(), actual.getComponentClass());

        ondemand.start();
        try {
            expected = rootContainer.getComponentDef("coolDto");
        } finally {
            ondemand.stop();
        }

        ondemand.start();
        try {
            actual = rootContainer.getComponentDef("coolDto");
        } finally {
            ondemand.stop();
        }

        assertSame(expected.getComponentClass(), actual.getComponentClass());
    }
}
