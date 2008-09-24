package org.seasar.cms.ymir.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.PathMappingProvider;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.mock.MockApplication;
import org.seasar.cms.ymir.mock.MockRequest;
import org.seasar.cms.ymir.response.ForwardResponse;
import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.env.Env;
import org.seasar.framework.mock.servlet.MockServletContextImpl;

public class DefaultRequestProcessorTest extends S2TestCase {

    private DefaultRequestProcessor target_;

    private PathMappingProvider pathMappingProvider_;

    protected void setUpContainer() throws Throwable {
        setServletContext(new MockServletContextImpl("test") {
            private static final long serialVersionUID = 1L;

            public Set getResourcePaths(String path) {
                if ("/path/to/".equals(path)) {
                    return new HashSet(Arrays.asList(new String[] {
                        "/path/to/file", "/path/to/dir/" }));
                } else if ("/".equals(path)) {
                    return new HashSet(Arrays.asList(new String[] { "/file",
                        "/dir/" }));
                } else {
                    return null;
                }
            }
        });
        super.setUpContainer();
    }

    protected void setUp() throws Exception {
        super.setUp();
        Env.setFilePath(ENV_PATH);
        // S2.4.11からモードがutにされてしまうようになったので…。
        Env.setValueIfAbsent(Env.PRODUCT);

        include("DefaultRequestProcessor.dicon");

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

    public void testFileResourceExists() throws Exception {

        assertTrue(target_.fileResourceExists("/file"));
        assertFalse(target_.fileResourceExists("/nonexistentfile"));
        assertFalse(target_.fileResourceExists("/dir"));

        assertTrue(target_.fileResourceExists("/path/to/file"));
        assertFalse(target_.fileResourceExists("/path/to/nonexistentfile"));
        assertFalse(target_.fileResourceExists("/path/to/dir"));

        assertFalse("親ディレクトリが存在しない場合もfalseを返すこと", target_
                .fileResourceExists("/nonexistentdir/file"));
    }
}
