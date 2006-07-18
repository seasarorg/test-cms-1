package org.seasar.cms.ymir.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.Configuration;
import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseProcessor;
import org.seasar.cms.ymir.Ymir;
import org.seasar.cms.ymir.container.hotdeploy.OndemandUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.log.Logger;

public class YmirImpl implements Ymir {

    private S2Container container_;

    private Configuration configuration_;

    private LifecycleListener[] lifecycleListeners_;

    private RequestProcessor requestProcessor_;

    private ResponseProcessor responseProcessor_;

    private Logger logger_ = Logger.getLogger(getClass());

    public void init() {

        logger_.debug("Ymir initialize start");

        initializeContainer();
        initializeListeners();

        logger_.debug("Ymir initialize end");
    }

    void initializeContainer() {

        String projectStatus = getConfiguration().getProperty(
            Configuration.KEY_PROJECTSTATUS);
        logger_.info("Project status is: "
            + (projectStatus != null ? projectStatus : "(UNDEFINED)"));

        // developモード以外の時はhotdeployを無効にするために
        // こうしている。
        if (!Configuration.PROJECTSTATUS_DEVELOP.equals(projectStatus)) {
            OndemandUtils.start(container_, true);
        }
    }

    void initializeListeners() {

        for (int i = 0; i < lifecycleListeners_.length; i++) {
            lifecycleListeners_[i].init();
        }
    }

    public Response processRequest(String contextPath, String path,
        String method, String dispatcher, Map parameterMap, Map fileParameterMap)
        throws PageNotFoundException {

        return requestProcessor_.process(contextPath, path, method, dispatcher,
            parameterMap, fileParameterMap);
    }

    public boolean processResponse(ServletContext servletContext,
        HttpServletRequest httpRequest, HttpServletResponse httpResponse,
        Response response) throws IOException, ServletException {

        return responseProcessor_.process(servletContext, httpRequest,
            httpResponse, response);
    }

    public void destroy() {

        logger_.debug("Ymir destroy start");

        destroyListeners();
        destroyContainer();
        configuration_ = null;
        requestProcessor_ = null;

        logger_.debug("Ymir destroy end");
    }

    void destroyListeners() {

        if (lifecycleListeners_ != null) {
            for (int i = 0; i < lifecycleListeners_.length; i++) {
                try {
                    lifecycleListeners_[i].destroy();
                } catch (Throwable t) {
                    logger_.error("Can't destroy lifecycleListener: "
                        + lifecycleListeners_[i], t);
                }
            }
            lifecycleListeners_ = null;
        }
    }

    void destroyContainer() {

        if (container_ != null) {
            if (!Configuration.PROJECTSTATUS_DEVELOP.equals(getConfiguration()
                .getProperty(Configuration.KEY_PROJECTSTATUS))) {

                OndemandUtils.stop(container_, true);
            }
            container_.destroy();
            container_ = null;
        }
    }

    public void setContainer(S2Container container) {

        container_ = container;
    }

    public Configuration getConfiguration() {

        return configuration_;
    }

    public void setConfiguration(Configuration configuration) {

        configuration_ = configuration;
    }

    public void setLifecycleListeners(LifecycleListener[] lifecycleListeners) {

        lifecycleListeners_ = lifecycleListeners;
    }

    public void setRequestProcessor(RequestProcessor requestProcessor) {

        requestProcessor_ = requestProcessor;
    }

    public void setResponseProcessor(ResponseProcessor responseProcessor) {

        responseProcessor_ = responseProcessor;
    }
}
