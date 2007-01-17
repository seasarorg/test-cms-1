package org.seasar.cms.ymir.response.scheme.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.SelfContainedResponse;
import org.seasar.kvasir.util.io.IOUtils;

import junit.framework.TestCase;

public class ContentStrategyTest extends TestCase {

    public void testConstructResponse1() throws Exception {

        Response actual = new ContentStrategy().constructResponse("body", null);

        assertEquals(SelfContainedResponse.DEFAULT_ASCII_CONTENTTYPE, actual
                .getContentType());
        assertEquals("body", IOUtils.readString(actual.getInputStream(),
                "UTF-8", false));
    }

    public void testConstructResponse2() throws Exception {

        Response actual = new ContentStrategy().constructResponse(
                "text/html; charset=ISO-2022-JP:body", null);

        assertEquals("text/html; charset=ISO-2022-JP", actual.getContentType());
        assertEquals("body", IOUtils.readString(actual.getInputStream(),
                "UTF-8", false));
    }

    public void testConstructResponse3() throws Exception {

        Response actual = new ContentStrategy().constructResponse(
                "This is text: TEXT.", null);

        assertEquals(SelfContainedResponse.DEFAULT_ASCII_CONTENTTYPE, actual
                .getContentType());
        assertEquals("This is text: TEXT.", IOUtils.readString(actual
                .getInputStream(), "UTF-8", false));
    }
}
