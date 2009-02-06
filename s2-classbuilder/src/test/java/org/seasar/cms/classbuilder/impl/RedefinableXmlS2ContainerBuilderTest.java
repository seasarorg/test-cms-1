package org.seasar.cms.classbuilder.impl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.env.Env;
import org.seasar.framework.util.ResourceUtil;

public class RedefinableXmlS2ContainerBuilderTest extends S2TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Env.setFilePath(ENV_PATH);
        // S2.4.11からモードがutにされてしまうようになったので…。
        Env.setValueIfAbsent(Env.PRODUCT);
    }

    public void test_コンポーネントの再定義ができること() throws Exception {
        include("test1.dicon");
        Hoe hoe = (Hoe) getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }

    public void test_コンポーネント名が同じコンポーネント定義は再定義されてそれ以外のコンポーネント定義があれば追加されること()
            throws Exception {
        include("test5.dicon");
        Fuga fuga = (Fuga) getComponent(Fuga.class);
        assertTrue(fuga instanceof FugaImpl2);
        assertTrue(getContainer().hasComponentDef(Hoe.class));
        assertNotNull("きちんとDIもされること", fuga.getHoe());
    }

    public void test_コンポーネントの追加ができること() throws Exception {
        include("test2.dicon");
        assertNotNull(getComponent(Hoe.class));
    }

    public void test_複数diconファイルからのコンポーネントの追加ができること() throws Exception {
        URL url = new File(ResourceUtil.getBuildDir(
                RedefinableXmlS2ContainerBuilderTest.class).getParentFile()
                .getParentFile(), "src/test/resources2").toURI().toURL();
        ClassLoader cl = new URLClassLoader(new URL[] { url }, getClass()
                .getClassLoader());

        S2Container container = S2ContainerFactory.create(cl.getResource(
                "test7.dicon").toExternalForm(), cl);

        assertTrue(container.hasComponentDef("hoe1"));
        assertTrue(container.hasComponentDef("hoe2"));
        assertTrue(container.hasComponentDef("hoe3"));
    }

    public void test_コンポーネントの除去ができること() throws Exception {
        include("test4.dicon");
        assertFalse(getContainer().hasComponentDef(Hoe.class));
    }

    public void test_JARの外にある差分diconによってJARに入っているdiconファイルのコンポーネントの再定義ができること()
            throws Exception {
        ClassLoader cl = new URLClassLoader(new URL[] { new URL("jar:"
                + ResourceUtil.getResource("testinjar1.jar").toExternalForm()
                + "!/") }, getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create("testinjar1.dicon",
                cl);
        Hoe hoe = (Hoe) container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }

    public void test_diconのパスがURLであってもJARの外にある差分diconによってJARに入っているdiconファイルのコンポーネントの再定義ができること()
            throws Exception {
        String jarPath = "jar:"
                + ResourceUtil.getResource("testinjar1.jar").toExternalForm()
                + "!/";
        ClassLoader cl = new URLClassLoader(new URL[] { new URL(jarPath) },
                getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create(jarPath
                + "testinjar1.dicon", cl);
        Hoe hoe = (Hoe) container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }

    public void test_JARの外にある差分diconによってJARに入っているdiconファイルの追加定義ができること()
            throws Exception {
        ClassLoader cl = new URLClassLoader(new URL[] { new URL("jar:"
                + ResourceUtil.getResource("testinjar2.jar").toExternalForm()
                + "!/") }, getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create("testinjar2.dicon",
                cl);
        assertNotNull(container.getComponent(Hoe.class));
    }

    public void test_diconのパスがURLであってもJARの外にある差分diconによってJARに入っているdiconファイルの追加定義ができること()
            throws Exception {
        String jarPath = "jar:"
                + ResourceUtil.getResource("testinjar2.jar").toExternalForm()
                + "!/";
        ClassLoader cl = new URLClassLoader(new URL[] { new URL(jarPath) },
                getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create(jarPath
                + "testinjar2.dicon", cl);
        assertNotNull(container.getComponent(Hoe.class));
    }

    public void test_diconファイル全体の置き換えができること() throws Exception {
        include("test3.dicon");
        Hoe hoe = (Hoe) getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }

    public void test_JARの外にある差分diconによってJARに入っているdiconファイル全体の置き換えができること()
            throws Exception {
        ClassLoader cl = new URLClassLoader(new URL[] { new URL("jar:"
                + ResourceUtil.getResource("testinjar3.jar").toExternalForm()
                + "!/") }, getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create("testinjar3.dicon",
                cl);
        Hoe hoe = (Hoe) container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }

    public void test_diconのパスがURLであってもJARの外にある置き換えdiconによってJARに入っているdiconファイル全体の置き換えができること()
            throws Exception {
        String jarPath = "jar:"
                + ResourceUtil.getResource("testinjar3.jar").toExternalForm()
                + "!/";
        ClassLoader cl = new URLClassLoader(new URL[] { new URL(jarPath) },
                getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create(jarPath
                + "testinjar3.dicon", cl);
        Hoe hoe = (Hoe) container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }

    public void test_違うディレクトリにある差分diconによってdiconファイルの追加定義ができること()
            throws Exception {
        URL url = new File(ResourceUtil.getBuildDir(
                RedefinableXmlS2ContainerBuilderTest.class).getParentFile()
                .getParentFile(), "src/test/resources2").toURI().toURL();
        ClassLoader cl = new URLClassLoader(new URL[] { url }, getClass()
                .getClassLoader());

        S2Container container = S2ContainerFactory.create(cl.getResource(
                "test6.dicon").toExternalForm(), cl);
        assertTrue(container.hasComponentDef("fuga2"));
    }

    public void test_コンポーネントの再定義と追加が同時にできること() throws Exception {
        include("test8.dicon");

        Hoe hoe = (Hoe) getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
        Fuga fuga = (Fuga) getComponent(Fuga.class);
        assertSame(hoe, fuga.getHoe());
    }

    public void test_コンポーネントを先頭に追加できること() throws Exception {
        include("test9.dicon");

        Hoe[] actual = (Hoe[]) getContainer().findAllComponents(Hoe.class);
        int idx = 0;
        assertEquals(2, actual.length);
        assertEquals("added", actual[idx++].getName());
        assertEquals("original", actual[idx++].getName());
    }

    public void test_コンポーネントを先頭と末尾に同時に追加できること() throws Exception {
        include("test10.dicon");

        Hoe[] actual = (Hoe[]) getContainer().findAllComponents(Hoe.class);
        int idx = 0;
        assertEquals(3, actual.length);
        assertEquals("prefix", actual[idx++].getName());
        assertEquals("original", actual[idx++].getName());
        assertEquals("suffix", actual[idx++].getName());
    }

    public void test_コンポーネントの差し替え時にincludeタグも考慮されること() throws Exception {
        include("test11.dicon");

        Hoe actual = (Hoe) getContainer().getComponent(Hoe.class);

        assertNotNull(actual.getFuga());

        boolean include1Found = false;
        boolean include2Found = false;
        S2Container container = getContainer().getChild(0);
        for (int i = 0; i < container.getChildSize(); i++) {
            S2Container child = container.getChild(i);
            if ("test11Include1.dicon".equals(child.getPath())) {
                include1Found = true;
            } else if ("test11Include2.dicon".equals(child.getPath())) {
                include2Found = true;
            }
        }
        assertTrue(include1Found);
        assertTrue(include2Found);
    }

    public void test_コンポーネントの追加にincludeタグも追加されること() throws Exception {
        include("test12.dicon");

        Hoe actual = (Hoe) getContainer().getComponent(Hoe.class);

        assertNotNull(actual.getFuga());

        boolean include1Found = false;
        boolean include2Found = false;
        S2Container container = getContainer().getChild(0);
        for (int i = 0; i < container.getChildSize(); i++) {
            S2Container child = container.getChild(i);
            if ("test12Include1.dicon".equals(child.getPath())) {
                include1Found = true;
            } else if ("test12Include2.dicon".equals(child.getPath())) {
                include2Found = true;
            }
        }
        assertTrue(include1Found);
        assertTrue(include2Found);
    }
}
