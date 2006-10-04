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
                return "/article/update.zpt";
            }

            public String getComponentName() {
                return "articlePage";
            }
        };
        Response response = new ForwardResponse("/article/update.zpt");

        Response actual = target_
                .normalizeResponse(response, request.getPath());

        assertEquals("リクエストパスと同じパスへのフォワードはPASSTHROUGHに正規化されること",
                Response.TYPE_PASSTHROUGH, actual.getType());
    }
}
