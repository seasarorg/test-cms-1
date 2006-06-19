package org.seasar.cms.framework.creator.impl;

import org.seasar.cms.framework.Request;

public class SourceCreatorImplTest extends SourceCreatorImplTestBase {

    public void testGetComponentName() throws Exception {

        String actual = target_.getComponentName("/index.html",
            Request.METHOD_GET);

        assertEquals("indexPage", actual);
    }

    public void testGetClassName1() throws Exception {

        assertNull(target_.getClassName(null));
    }

    public void testGetClassName2() throws Exception {

        String actual = target_.getClassName("indexPage");

        assertEquals("com.example.web.IndexPage", actual);
    }

    public void testGetWelcomeFile() throws Exception {

        assertEquals("index.html", target_.getWelcomeFile());
    }
}
