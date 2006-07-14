package org.seasar.cms.ymir.container;

import java.net.URL;

import junit.framework.TestCase;

public class YmirPathResolverTest extends TestCase {

    public void testGetResourceURL() throws Exception {

        // ## Arrange ##
        URL[] urls = new URL[] {
            new URL("jar:file:/path/to/jar/test-0.0.1.jar!/"),
            new URL("jar:file:/path/to/jar/hoge-0.0.1.jar!/"), };

        // ## Act ##
        URL actual = new YmirPathResolver().getResourceURL("test", urls);

        // ## Assert ##
        assertEquals(urls[0], actual);

        // ## Act ##
        actual = new YmirPathResolver().getResourceURL("hoe", urls);

        // ## Assert ##
        assertNull(actual);
    }
}
