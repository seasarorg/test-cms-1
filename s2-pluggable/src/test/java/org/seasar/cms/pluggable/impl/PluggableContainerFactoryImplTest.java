package org.seasar.cms.pluggable.impl;

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.seasar.cms.pluggable.Listener;
import org.seasar.cms.pluggable.OneListener;
import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.TooManyRegistrationRuntimeException;
import org.seasar.framework.container.factory.CircularIncludeRuntimeException;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.impl.S2ContainerImpl;
import org.seasar.framework.util.ResourceUtil;

public class PluggableContainerFactoryImplTest extends TestCase {

    private PluggableContainerFactoryImpl target_;

    private URL[] resourceURLs_;

    protected void setUp() throws Exception {
        super.setUp();

        target_ = new PluggableContainerFactoryImpl();
        SingletonPluggableContainerFactory.setInstance(target_);
        resourceURLs_ = new URL[] {
            new File(ResourceUtil.getBuildDir(getClass()),
                    "org/seasar/cms/pluggable/listener1.dicon").toURI().toURL(),
            new File(ResourceUtil.getBuildDir(getClass()),
                    "org/seasar/cms/pluggable/listener2.dicon").toURI().toURL(), };
    }

    public void testIntegrate1() throws Exception {

        // ## Arrange ##
        S2Container root = new S2ContainerImpl();
        S2Container container = new S2ContainerImpl();
        root.include(container);
        S2Container dependency = new S2ContainerImpl();
        root.include(dependency);
        target_.setRootContainer(root);

        // ## Act ##
        target_.integrate(root, container, new S2Container[] { dependency },
                resourceURLs_);

        // ## Assert ##
        assertEquals("rootの子はcontainerとdependency", 2, root.getChildSize());
        assertSame("最初の子はcontainer", container, root.getChild(0));
        assertEquals(
                "includedがcontainerの子になる（それによってcontainerはleafではなくなり、直接dependencyを子として持たなくなる）",
                1, container.getChildSize());
        assertEquals("2番目の子はincluded", ResourceUtil.getResourceNoException(
                "org/seasar/cms/pluggable/included.dicon").toExternalForm(),
                container.getChild(0).getPath());
        assertSame("includedのルートはrootになる", root, container.getChild(0)
                .getRoot());
    }

    public void testIntegrate2() throws Exception {

        // ## Arrange ##
        S2Container root = new S2ContainerImpl();
        S2Container dependency = new S2ContainerImpl();
        dependency.register(String.class);
        root.include(dependency);
        target_.setRootContainer(root);

        // ## Act ##
        S2Container actual = target_.integrate(null,
                new S2Container[] { dependency });

        // ## Assert ##
        assertEquals("configPathがnullの場合は空のコンテナインスタンスを割り当ててくれること", 0, actual
                .getComponentDefSize());
        assertEquals(2, root.getChildSize());
        assertSame(root.getChild(1), actual);
        assertEquals("", root.getChild(1).getComponent(String.class));
    }

    public void testIntegrate3() throws Exception {

        // ## Arrange ##
        S2Container root = new S2ContainerImpl();
        S2Container container = new S2ContainerImpl();
        root.include(container);
        target_.setRootContainer(root);

        // ## Act ##
        target_.integrate(root, container, new S2Container[0],
                new URL[] { getClass().getClassLoader().getResource(
                        getClass().getName().replace('.', '/')
                                + "_testIntegrate3.dicon") });

        // ## Assert ##
        assertEquals("統合されるdiconファイルsg内でincludeされているコンテナも統合されること", 1, container
                .findAllComponents(List.class).length);
    }

    public void testIntegrate4() throws Exception {

        target_.prepareForContainer();
        S2Container container1 = target_.integrate(getClass().getName()
                .replace('.', '/')
                + "_testIntegrate4_1.dicon", new S2Container[0]);
        target_.integrate(getClass().getName().replace('.', '/')
                + "_testIntegrate4_2.dicon", new S2Container[] { container1 });
        assertEquals("一度invokeAutoRegister()されたコンテナについては再度registerAll()されないこと",
                1, ((MockAutoRegister) container1
                        .getComponent(MockAutoRegister.class)).getCount());
    }

    public void testIntegrate5() throws Exception {

        target_.prepareForContainer();
        S2Container actual = target_.integrate(getClass().getName().replace(
                '.', '/')
                + "_testIntegrate5.dicon", new S2Container[0]);
        assertTrue("expand対象のcontainerの中身が展開されること", actual
                .hasComponentDef(List.class));
    }

    public void testIntegrate6() throws Exception {

        target_.prepareForContainer();
        S2Container included = target_.createS2Container(getClass()
                .getPackage().getName().replace('.', '/')
                + "/included6.dicon");
        target_.getRootContainer().include(included);
        target_.getRootContainer().registerDescendant(included);

        S2Container actual = target_.integrate(getClass().getName().replace(
                '.', '/')
                + "_testIntegrate6.dicon", new S2Container[0]);

        assertSame(
                "expand対象の中でincludeされているものが既にrootにincludeされていたら同じオブジェクトを指すようになること",
                included, actual.getChild(0));
    }

    public void testIntegrate7() throws Exception {

        target_.prepareForContainer();
        S2Container actual = target_.integrate(getClass().getName().replace(
                '.', '/')
                + "_testIntegrate7.dicon", new S2Container[0]);

        assertEquals("同じものを2回expandした場合でも両方とも正しく展開されること", 2, actual
                .findAllComponentDefs(List.class).length);
    }

    public void testIntegrate8() throws Exception {

        target_.prepareForContainer();
        S2Container actual = target_.integrate(getClass().getName().replace(
                '.', '/')
                + "_testIntegrate8.dicon", new S2Container[0]);
        assertTrue("expandが再帰的に展開されること", actual.hasComponentDef(List.class));
    }

    /*
     * S2Container#findComponents()はコンテナをまたがってコンポーネントを
     * 収集しないという仕様とのこと。
     */
    public void testSpike1() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
                .create("org/seasar/cms/pluggable/spike1.dicon");

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
            S2ContainerFactory.create("org/seasar/cms/pluggable/spike2.dicon");
            fail("循環参照エラーになること");
        } catch (CircularIncludeRuntimeException expected) {
        }
    }

    public void testSpike3() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
                .create("org/seasar/cms/pluggable/spike3.dicon");

        // ## Act ##
        // ## Assert ##
        try {
            S2ContainerFactory.include(container,
                    "org/seasar/cms/pluggable/spike3.dicon");
            fail("循環参照エラーになること");
        } catch (CircularIncludeRuntimeException expected) {
        }
    }

    public void testSpike4() throws Exception {

        // ## Arrange ##
        S2Container container = S2ContainerFactory
                .create("org/seasar/cms/pluggable/spike4.dicon");

        // ## Act ##
        // ## Assert ##
        try {
            container.getComponent(List.class);
        } catch (TooManyRegistrationRuntimeException ex) {
            fail("同じdiconを複数インクルードしていてもコンテナとしては1つとして扱われること");
        }
    }

    public void testIncludeToLeaves1() throws Exception {

        S2Container root = new S2ContainerImpl();

        S2Container container = new S2ContainerImpl();
        container.setRoot(root);
        S2Container leaf = new S2ContainerImpl();
        container.include(leaf);

        S2Container dep = new S2ContainerImpl();
        dep.setRoot(root);
        dep.include(leaf);

        try {
            target_.includeToLeaves(container, new S2Container[] { dep });
        } catch (CircularIncludeRuntimeException ex) {
            fail("循環参照を起こすようなincludeはしないこと");
        }
    }
}
