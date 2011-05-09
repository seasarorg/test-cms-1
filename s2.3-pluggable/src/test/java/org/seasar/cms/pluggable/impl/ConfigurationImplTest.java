package org.seasar.cms.pluggable.impl;

import java.util.Enumeration;

import junit.framework.TestCase;

public class ConfigurationImplTest extends TestCase {
    private ConfigurationImpl target = new ConfigurationImpl();

    public void test_指定した順にリソースが優先されること() throws Exception {
        target.load(new String[] {
            getClass().getResource("ConfigurationImplTest_a.properties")
                    .toExternalForm(),
            getClass().getResource("ConfigurationImplTest_b.properties")
                    .toExternalForm() });

        assertEquals("HOE_A", target.getProperty("hoe"));
        assertEquals("FUGA_B", target.getProperty("fuga"));
    }

    public void test_ロードするコンフィグファイルが0個の場合に空の状態が保たれること() throws Exception {
        target.load(new String[0]);

        int count = 0;
        for (Enumeration<String> enm = target.propertyNames(); enm
                .hasMoreElements(); enm.nextElement()) {
            count++;
        }
        assertEquals(0, count);
    }
}
