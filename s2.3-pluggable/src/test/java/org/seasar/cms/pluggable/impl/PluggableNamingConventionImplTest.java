package org.seasar.cms.pluggable.impl;

import junit.framework.TestCase;

public class PluggableNamingConventionImplTest extends TestCase {
    private PluggableNamingConventionImpl target_;

    protected void setUp() throws Exception {
        super.setUp();
        target_ = new PluggableNamingConventionImpl();
        target_.addRootPackageName("com.example");
    }

    public void testFromClassNameToSuffix() throws Exception {
        assertNull(target_.fromClassNameToSuffix("com.examplePages.page"));

        assertNull(target_.fromClassNameToSuffix("com.example.page"));
    }

    public void testFromClassNameToShortComponentName() throws Exception {
        assertEquals(
                "testPage",
                target_
                        .fromClassNameToShortComponentName("com.example.impl.TestPageImpl"));

        assertEquals("testPage", target_
                .fromClassNameToShortComponentName("com.example.TestPage"));

        assertEquals("aPage", target_
                .fromClassNameToShortComponentName("com.example.APage"));
    }

    public void test_キャメルスタイルのパス名に対応するコンポーネント名をクラス名に変換して再度コンポーネント名に戻しても元の名前になること()
            throws Exception {
        String componentName = "pagePage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));

        componentName = "pathToPagePage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));

        componentName = "pathToAPage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));
    }

    public void test_アンダースコア連結スタイルのパス名に対応するコンポーネント名をクラス名に変換して再度コンポーネント名に戻しても元の名前になること()
            throws Exception {
        String componentName = "pagePage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));

        componentName = "path_to_pagePage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));

        componentName = "path_to_aPage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));
    }

    public void test_混在スタイルのパス名に対応するコンポーネント名をクラス名に変換して再度コンポーネント名に戻しても元の名前になること()
            throws Exception {
        String componentName = "pathTo_pagePagePage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));

        componentName = "path_toThe_pageAPage";
        assertEquals(componentName, target_
                .fromClassNameToComponentName(target_
                        .fromComponentNameToClassName(componentName)));
    }
}
