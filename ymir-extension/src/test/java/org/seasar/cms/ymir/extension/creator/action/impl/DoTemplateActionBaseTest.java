package org.seasar.cms.ymir.extension.creator.action.impl;

import junit.framework.TestCase;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.mock.MockSourceCreator;
import org.seasar.cms.ymir.extension.mock.MockHttpServletRequest;
import org.seasar.cms.ymir.mock.MockRequest;

public class DoTemplateActionBaseTest extends TestCase {

    private DoTemplateActionBase target_ = new DoTemplateActionBase(
            new MockSourceCreator()
                    .setHttpServletRequest(new MockHttpServletRequest()
                            .setRequestURL(
                                    new StringBuffer(
                                            "http://localhost:8080/context/action/hogehoge.do"))
                            .setRequestURI("/context/action/hogehoge.do"))) {
        public Response act(Request request, PathMetaData pathMetaData) {
            return null;
        }
    };

    public void testGetPath() throws Exception {

        assertEquals("/index.html", target_
                .getPath(buildMock("http://localhost:8080/context/index.html")));
    }

    public void testGetPath_URLにリクエストパラメータがついていても正しくパスが取得できること()
            throws Exception {

        assertEquals("/index.html", target_
                .getPath(buildMock("http://localhost:8080/context/index.html?a=1")));
    }

    private MockRequest buildMock(final String url) {
        return new MockRequest() {
            @Override
            public String getParameter(String name) {
                if (name.equals("path")) {
                    return url;
                } else {
                    return null;
                }
            }
        }.setContextPath("/context");
    }
}
