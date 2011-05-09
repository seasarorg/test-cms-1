package org.seasar.cms.pluggable.impl;

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
}
