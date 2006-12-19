package org.seasar.cms.classbuilder.impl;

import java.util.List;
import java.util.Map;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.container.ComponentDef;


public class ClassS2ContainerBuilderTest extends S2TestCase
{
    public void test_Preparerを読み込めること()
        throws Exception
    {
        include(AppPreparer1.class.getName().replace('.', '/').concat(".class"));
    }


    public void test_読み込んだPreparerはコンテナにセットされること()
        throws Exception
    {
        include(AppPreparer1.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(AppPreparer1.class));
    }


    public void test_読み込んだPreparerにはコンテナがセットされていること()
        throws Exception
    {
        include(AppPreparer1.class.getName().replace('.', '/').concat(".class"));
        S2ContainerPreparer preparer = (S2ContainerPreparer)getContainer()
            .getComponent(AppPreparer1.class);
        assertNotNull(preparer.getContainer());
    }


    public void test_Preparerを読み込んで正しくコンテナを構築できること()
        throws Exception
    {
        include(AppPreparer2.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(Hoe.class));
        assertEquals("hoe", ((Hoe)getComponent(Hoe.class)).getName());
    }


    public void test_相互参照しているコンポーネントを構築できること()
        throws Exception
    {
        include(AppPreparer3.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(Hoe.class));
        Hoe hoe = (Hoe)getComponent(Hoe.class);
        assertTrue(getContainer().hasComponentDef(FugaImpl.class));
        Fuga fuga = (Fuga)getComponent(Fuga.class);
        assertSame(fuga, hoe.getFuga());
        assertSame(hoe, fuga.getHoe());
    }


    public void test_明示的なセットによって相互参照しているコンポーネントを構築できること()
        throws Exception
    {
        include(AppPreparer4.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(Hoe2.class));
        Hoe2 hoe2 = (Hoe2)getComponent(Hoe2.class);
        assertTrue(getContainer().hasComponentDef(Fuga2.class));
        Fuga2 fuga2 = (Fuga2)getComponent(Fuga2.class);
        assertSame(fuga2, hoe2.getFuga2());
        assertSame(hoe2, fuga2.getHoe2());
    }


    public void test_正しくインクルードできること()
        throws Exception
    {
        include(AppPreparer5.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(Hoe3.class));
        Hoe3 hoe3 = (Hoe3)getComponent(Hoe3.class);
        assertTrue(getContainer().hasComponentDef(Fuga3.class));
        Fuga3 fuga3 = (Fuga3)getComponent(Fuga3.class);
        assertSame(fuga3, hoe3.getFuga3());
    }


    public void test_指定されたプロパティを自動バインディング対象から外せること()
        throws Exception
    {
        include(AppPreparer6.class.getName().replace('.', '/').concat(".class"));
        Hoe4 hoe4 = (Hoe4)getComponent(Hoe4.class);
        assertNotNull(hoe4.getGuu1());
        assertNull(hoe4.getGuu2());
    }


    public void test_instance指定ができること()
        throws Exception
    {
        include(AppPreparer6.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(List.class));
        ComponentDef componentDef = getComponentDef(List.class);
        assertEquals("prototype", componentDef.getInstanceDef().getName());
    }


    public void test_aspectがかけられること()
        throws Exception
    {
        include(AppPreparer6.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(Map.class));
        Map map = (Map)getComponent(Map.class);
        assertEquals("intercept!", map.get(null));
    }


    public void test_コンストラクタインジェクションができること()
        throws Exception
    {
        include(AppPreparer7.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(Hoe5.class));
        Hoe5 hoe5 = (Hoe5)getComponent(Hoe5.class);
        assertSame(getComponent(Fuga5.class), hoe5.getFuga5());
    }


    public void test_コンポーネントのコンストラクタを指定できること()
        throws Exception
    {
        include(AppPreparer8.class.getName().replace('.', '/').concat(".class"));
        assertTrue(getContainer().hasComponentDef(Hoe6.class));
        Hoe6 hoe6 = (Hoe6)getComponent(Hoe6.class);
        assertNotNull(hoe6.getFuga6());
    }
}
