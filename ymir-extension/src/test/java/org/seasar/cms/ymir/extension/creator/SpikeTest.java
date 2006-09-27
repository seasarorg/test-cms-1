package org.seasar.cms.ymir.extension.creator;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import junit.framework.TestCase;

public class SpikeTest extends TestCase {

    public void testBeanInfo() throws Exception {

        BeanInfo beanInfo = Introspector.getBeanInfo(Hoe.class);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        assertEquals("返り値がvoidでないsetterはプロパティとみなされない", 1, pds.length);
        assertEquals("class", pds[0].getName());
    }

    public static class Hoe {
        public String setHoe(String hoe) {
            return "hoe";
        }
    }
}
