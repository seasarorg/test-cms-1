package org.seasar.cms.ymir.impl;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.ExceptionProcessor;
import org.seasar.cms.ymir.HttpServletResponseFilter;
import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.PermissionDeniedException;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseProcessor;
import org.seasar.cms.ymir.Ymir;
import org.seasar.framework.util.Disposable;
import org.seasar.framework.util.DisposableUtil;

public class YmirImpl implements Ymir {

    private LifecycleListener[] lifecycleListeners_;

    private Configuration configuration_;

    private ApplicationManager applicationManager_;

    private RequestProcessor requestProcessor_;

    private ResponseProcessor responseProcessor_;

    private ExceptionProcessor exceptionProcessor_;

    private Log log_ = LogFactory.getLog(getClass());

    public void setLifecycleListeners(LifecycleListener[] lifecycleListeners) {

        lifecycleListeners_ = lifecycleListeners;
    }

    public void setRequestProcessor(RequestProcessor requestProcessor) {

        requestProcessor_ = requestProcessor;
    }

    public void setResponseProcessor(ResponseProcessor responseProcessor) {

        responseProcessor_ = responseProcessor;
    }

    public void setExceptionProcessor(ExceptionProcessor exceptionProcessor) {

        exceptionProcessor_ = exceptionProcessor;
    }

    public void setConfiguration(Configuration configuration) {

        configuration_ = configuration;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {

        applicationManager_ = applicationManager;
    }

    public void init() {

        log_.debug("Ymir initialize start");

        initializeListeners();

        DisposableUtil.add(new Disposable() {
            public void dispose() {
                PropertyUtils.clearDescriptors();
            }
        });

        log_.debug("Ymir initialize end");
    }

    void initializeListeners() {

        for (int i = 0; i < lifecycleListeners_.length; i++) {
            lifecycleListeners_[i].init();
        }
    }

    public Request prepareForProcessing(String contextPath, String path,
            String method, String dispatcher, Map parameterMap,
            Map fileParameterMap, AttributeContainer attributeContainer,
            Locale locale) {

        return requestProcessor_.prepareForProcessing(contextPath, path, method
                .toUpperCase(), dispatcher, parameterMap, fileParameterMap,
                attributeContainer, locale);
    }

    public Response processRequest(Request request)
            throws PageNotFoundException, PermissionDeniedException {

        return requestProcessor_.process(request);
    }

    public HttpServletResponseFilter processResponse(
            ServletContext servletContext, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, Request request, Response response)
            throws IOException, ServletException {

        return responseProcessor_.process(servletContext, httpRequest,
                httpResponse, request, response);
    }

    public void destroy() {

        log_.debug("Ymir destroy start");

        destroyListeners();
        requestProcessor_ = null;

        log_.debug("Ymir destroy end");
    }

    void destroyListeners() {

        if (lifecycleListeners_ != null) {
            for (int i = 0; i < lifecycleListeners_.length; i++) {
                try {
                    lifecycleListeners_[i].destroy();
                } catch (Throwable t) {
                    log_.error("Can't destroy lifecycleListener: "
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

    public Application getApplication() {

        return applicationManager_.findContextApplication();
    }

    public String getProjectStatus() {

        if (configuration_ != null) {
            return configuration_.getProperty(Configuration.KEY_PROJECTSTATUS);
        } else {
            return null;
        }
    }

    public boolean isUnderDevelopment() {

        return Configuration.PROJECTSTATUS_DEVELOP.equals(getProjectStatus())
                && getApplication().isUnderDevelopment();
    }

    public Response processException(Request request, Throwable t) {

        return exceptionProcessor_.process(request, t);
    }
}
