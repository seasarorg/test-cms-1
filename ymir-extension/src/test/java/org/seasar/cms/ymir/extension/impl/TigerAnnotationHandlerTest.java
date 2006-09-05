package org.seasar.cms.ymir.extension.impl;

import junit.framework.TestCase;

public class TigerAnnotationHandlerTest extends TestCase {

    public void testToAttributeName() throws Exception {

        assertEquals("attributeName", new TigerAnnotationHandler()
                .toAttributeName("setAttributeName", ""));
        assertEquals("attribute", new TigerAnnotationHandler().toAttributeName(
                "attribute", ""));
        assertEquals("hoe", new TigerAnnotationHandler().toAttributeName(
                "setAttributeName", "hoe"));
        assertEquals("hoeFuga", new TigerAnnotationHandler().toAttributeName(
                "attribute", "hoeFuga"));
    }
}
