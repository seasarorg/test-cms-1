package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.PathMappingProvider;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.mock.MockApplication;
import org.seasar.cms.ymir.mock.MockRequest;
import org.seasar.cms.ymir.response.ForwardResponse;
import org.seasar.extension.unit.S2TestCase;

public class DefaultRequestProcessorTest extends S2TestCase {

    private DefaultRequestProcessor target_;

    private PathMappingProvider pathMappingProvider_;

    protected void setUp() throws Exception {
        super.setUp();

        include("DefaultRequestProcessor.dicon");
        getContainer().init();

        target_ = (DefaultRequestProcessor) getComponent(DefaultRequestProcessor.class);
        pathMappingProvider_ = (PathMappingProvider) getComponent(PathMappingProvider.class);

        ApplicationManager applicationManager = (ApplicationManager) getComponent(ApplicationManager.class);
        Application application = new MockApplication() {
            public PathMappingProvider getPathMappingProvider() {
                return pathMappingProvider_;
            }
        };
        applicationManager.setBaseApplication(application);
    }

    public void testNormlizeResponse() throws Exception {

        Request request = new MockRequest() {
            public String getPath() {
                return "/article/update.do";
            }

            public String getComponentName() {
                return "articlePage";
            }
        };
        Response response = new ForwardResponse("/article.done");

        Response actual = target_.normalizeResponse(response, request, null);

        assertEquals("同一のコンポーネントに対応するパスへのフォワードについては、そうでなくなるまで遷移先を読み飛ばすこと",
                Response.TYPE_FORWARD, actual.getType());
        assertEquals("同一のコンポーネントに対応するパスへのフォワードについては、そうでなくなるまで遷移先を読み飛ばすこと",
                "/article.html", actual.getPath());
    }

    public void testNormlizeResponse2() throws Exception {

        Request request = new MockRequest() {
            public String getPath() {
                return "/article.do";
            }

            public String getComponentName() {
                return "articlePage";
            }
        };
        Response response = new ForwardResponse("/article.html");

        Response actual = target_.normalizeResponse(response, request, null);

        assertEquals("同一のコンポーネントに対応するパスへのフォワードについては、そうでなくなるまで遷移先を読み飛ばすこと",
                Response.TYPE_FORWARD, actual.getType());
        assertEquals("同一のコンポーネントに対応するパスへのフォワードについては、そうでなくなるまで遷移先を読み飛ばすこと",
                "/article.html", actual.getPath());
    }

    public void testNormlizeResponse3() throws Exception {

        Request request = new MockRequest() {
            public String getPath() {
                return "/article.html";
            }

            public String getComponentName() {
                return "articlePage";
            }
        };
        Response response = new ForwardResponse("/article.html");

        Response actual = target_.normalizeResponse(response, request, null);

        assertEquals("同一のコンポーネントに対応するパスへのフォワードについて、"
                + "そうでなくなるまで遷移先を読み飛ばした結果がリクエストパスと同じ場合は"
                + "レスポンスがpassthroughになること", Response.TYPE_PASSTHROUGH, actual
                .getType());
    }
}
