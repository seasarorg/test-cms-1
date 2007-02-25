package org.seasar.cms.webdavjp;

import junit.framework.TestCase;

public class RequestURIDecodingRequestTest extends TestCase {

    public void testSrtipPathParameter() throws Exception {

        assertNull(RequestURIDecodingRequest.stripPathParameter(null));

        assertEquals("/path/to/file", RequestURIDecodingRequest
                .stripPathParameter("/path/to/file"));

        assertEquals("/path/to/file", RequestURIDecodingRequest
                .stripPathParameter("/path/to/file;a=b"));
    }
}
