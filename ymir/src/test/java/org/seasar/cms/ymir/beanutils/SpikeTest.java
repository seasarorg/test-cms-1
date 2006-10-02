package org.seasar.cms.ymir.beanutils;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;

public class SpikeTest extends TestCase {

    public void testConvert() throws Exception {

        Map properties = new HashMap();
        properties.put("integer", new String[] { "1" });
        properties.put("int", new String[] { "1" });
        TestBean2 actual = new TestBean2();
        BeanUtils.populate(actual, properties);

        assertEquals(new Integer(1), actual.getInteger());
        assertEquals(1, actual.getInt());

        properties.put("integer", new String[] { "a" });
        properties.put("int", new String[] { "a" });
        BeanUtils.populate(actual, properties);

        assertEquals("Integerに変換できない場合は0が設定される", new Integer(0), actual
                .getInteger());
        assertEquals("intに変換できない場合は0が設定される", 0, actual.getInt());

        properties.put("integer", new String[] { "" });
        properties.put("int", new String[] { "" });
        BeanUtils.populate(actual, properties);

        assertEquals("空文字列が指定された場合は0が設定される", new Integer(0), actual
                .getInteger());
        assertEquals("空文字列が指定された場合は0が設定される", 0, actual.getInt());
    }
}
