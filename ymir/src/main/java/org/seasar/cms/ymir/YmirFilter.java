package org.seasar.cms.ymir;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.util.ServletUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class YmirFilter implements Filter {

    private String dispatcher_;

    private ServletContext context_;

    public void init(FilterConfig config) throws ServletException {

        context_ = config.getServletContext();

        String dispatcher = config.getInitParameter("dispatcher");
        if (dispatcher == null) {
            throw new ServletException(
                "Init-param 'dispatcher' must be specified");
        }
        dispatcher_ = dispatcher.toUpperCase();
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res,
        FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;

        Map fileParameterMap = (Map) httpRequest
            .getAttribute(MultipartServletRequest.ATTR_FORMFILEMAP);
        if (fileParameterMap != null) {
            httpRequest
                .removeAttribute(MultipartServletRequest.ATTR_FORMFILEMAP);
        } else {
            fileParameterMap = new HashMap();
        }

        try {
            Response response = processRequest(ServletUtils
                .getContextPath(httpRequest),
                ServletUtils.getPath(httpRequest), httpRequest.getMethod()
                    .toUpperCase(), dispatcher_, httpRequest.getParameterMap(),
                fileParameterMap);

            if (processResponse(httpRequest, httpResponse, response)) {
                chain.doFilter(httpRequest, httpResponse);
            }
        } catch (PageNotFoundException ex) {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    Response processRequest(String contextPath, String path, String method,
        String dispatcher, Map parameterMap, Map fileParameterMap)
        throws PageNotFoundException {

        return ((RequestProcessor) getContainer().getComponent(
            RequestProcessor.class)).process(contextPath, path, method,
            dispatcher, parameterMap, fileParameterMap);
    }

    boolean processResponse(HttpServletRequest httpRequest,
        HttpServletResponse httpResponse, Response response)
        throws IOException, ServletException {

        return ((ResponseProcessor) getContainer().getComponent(
            ResponseProcessor.class)).process(context_, httpRequest,
            httpResponse, response);
    }

    S2Container getContainer() {

        return SingletonS2ContainerFactory.getContainer();
    }
}
