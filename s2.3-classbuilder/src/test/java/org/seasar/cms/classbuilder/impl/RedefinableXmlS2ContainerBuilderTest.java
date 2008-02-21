package org.seasar.cms.classbuilder.impl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.util.ResourceUtil;


public class RedefinableXmlS2ContainerBuilderTest extends S2TestCase
{
    public void test_コンポーネントの再定義ができること()
        throws Exception
    {
        include("test1.dicon");
        Hoe hoe = (Hoe)getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }


    public void test_コンポーネント名が同じコンポーネント定義は再定義されてそれ以外のコンポーネント定義があれば追加されること()
        throws Exception
    {
        include("test5.dicon");
        Fuga fuga = (Fuga)getComponent(Fuga.class);
        assertTrue(fuga instanceof FugaImpl2);
        assertTrue(getContainer().hasComponentDef(Hoe.class));
        assertNotNull("きちんとDIもされること", fuga.getHoe());
    }


    public void test_コンポーネントの追加ができること()
        throws Exception
    {
        include("test2.dicon");
        assertNotNull(getComponent(Hoe.class));
    }


    public void test_コンポーネントの除去ができること()
        throws Exception
    {
        include("test4.dicon");
        assertFalse(getContainer().hasComponentDef(Hoe.class));
    }


    public void test_JARの外にある差分diconによってJARに入っているdiconファイルのコンポーネントの再定義ができること()
        throws Exception
    {
        ClassLoader cl = new URLClassLoader(new URL[] { new URL("jar:"
            + ResourceUtil.getResource("testinjar1.jar").toExternalForm()
            + "!/") }, getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create("testinjar1.dicon",
            cl);
        Hoe hoe = (Hoe)container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }


    public void test_diconのパスがURLであってもJARの外にある差分diconによってJARに入っているdiconファイルのコンポーネントの再定義ができること()
        throws Exception
    {
        String jarPath = "jar:"
            + ResourceUtil.getResource("testinjar1.jar").toExternalForm()
            + "!/";
        ClassLoader cl = new URLClassLoader(new URL[] { new URL(jarPath) },
            getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create(jarPath
            + "testinjar1.dicon", cl);
        Hoe hoe = (Hoe)container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }


    public void test_JARの外にある差分diconによってJARに入っているdiconの追加定義ができること()
        throws Exception
    {
        ClassLoader cl = new URLClassLoader(new URL[] { new URL("jar:"
            + ResourceUtil.getResource("testinjar2.jar").toExternalForm()
            + "!/") }, getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create("testinjar2.dicon",
            cl);
        assertNotNull(container.getComponent(Hoe.class));
    }


    public void test_diconのパスがURLであってもJARの外にある差分diconによってJARに入っているdiconファイルの追加定義ができること()
        throws Exception
    {
        String jarPath = "jar:"
            + ResourceUtil.getResource("testinjar2.jar").toExternalForm()
            + "!/";
        ClassLoader cl = new URLClassLoader(new URL[] { new URL(jarPath) },
            getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create(jarPath
            + "testinjar2.dicon", cl);
        assertNotNull(container.getComponent(Hoe.class));
    }


    public void test_diconファイル全体の置き換えができること()
        throws Exception
    {
        include("test3.dicon");
        Hoe hoe = (Hoe)getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }


    public void test_JARの外にある差分diconによってJARに入っているdiconファイル全体の置き換えができること()
        throws Exception
    {
        ClassLoader cl = new URLClassLoader(new URL[] { new URL("jar:"
            + ResourceUtil.getResource("testinjar3.jar").toExternalForm()
            + "!/") }, getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create("testinjar3.dicon",
            cl);
        Hoe hoe = (Hoe)container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }


    public void test_diconのパスがURLであってもJARの外にある置き換えdiconによってJARに入っているdiconファイル全体の置き換えができること()
        throws Exception
    {
        String jarPath = "jar:"
            + ResourceUtil.getResource("testinjar3.jar").toExternalForm()
            + "!/";
        ClassLoader cl = new URLClassLoader(new URL[] { new URL(jarPath) },
            getClass().getClassLoader());
        S2Container container = S2ContainerFactory.create(jarPath
            + "testinjar3.dicon", cl);
        Hoe hoe = (Hoe)container.getComponent(Hoe.class);
        assertEquals("redefined", hoe.getName());
    }


    public void test_違うディレクトリにある差分diconによってdiconの追加定義ができること()
        throws Exception
    {
        URL url = new File(ResourceUtil.getBuildDir(
            RedefinableXmlS2ContainerBuilderTest.class).getParentFile()
            .getParentFile(), "src/test/resources2").toURI().toURL();
        ClassLoader cl = new URLClassLoader(new URL[] { url }, getClass()
            .getClassLoader());

        S2Container container = S2ContainerFactory.create(cl.getResource(
            "test6.dicon").toExternalForm(), cl);
        assertTrue(container.hasComponentDef("fuga2"));
    }
}
