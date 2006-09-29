package org.seasar.cms.ymir.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseProcessor;
import org.seasar.cms.ymir.PermissionDeniedExeption;
import org.seasar.cms.ymir.Ymir;
import org.seasar.framework.log.Logger;

public class YmirImpl implements Ymir {

    private LifecycleListener[] lifecycleListeners_;

    private RequestProcessor requestProcessor_;

    private ResponseProcessor responseProcessor_;

    private Logger logger_ = Logger.getLogger(getClass());

    public void init() {

        logger_.debug("Ymir initialize start");

        initializeListeners();

        logger_.debug("Ymir initialize end");
    }

    void initializeListeners() {

        for (int i = 0; i < lifecycleListeners_.length; i++) {
            lifecycleListeners_[i].init();
        }
    }

    public Request prepareForProcessing(String contextPath, String path,
            String method, String dispatcher, Map parameterMap,
            Map fileParameterMap, AttributeContainer attributeContainer)
            throws PageNotFoundException {

        return requestProcessor_.prepareForProcessing(contextPath, path, method
                .toUpperCase(), dispatcher, parameterMap, fileParameterMap,
                attributeContainer);
    }

    public Response processRequest(Request request) throws PermissionDeniedExeption {

        return requestProcessor_.process(request);
    }

    public boolean processResponse(ServletContext servletContext,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            Request request, Response response) throws IOException,
            ServletException {

        return responseProcessor_.process(servletContext, httpRequest,
                httpResponse, request, response);
    }

    public void destroy() {

        logger_.debug("Ymir destroy start");

        destroyListeners();
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

    public Object backupForInclusion(AttributeContainer attributeContainer) {

        return requestProcessor_.backupForInclusion(attributeContainer);
    }

    public void restoreForInclusion(AttributeContainer attributeContainer,
            Object backupped) {

        requestProcessor_.restoreForInclusion(attributeContainer, backupped);
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
