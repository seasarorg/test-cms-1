package org.seasar.cms.framework;

import java.io.IOException;
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

import org.seasar.cms.framework.util.ServletUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class FrameworkFilter implements Filter {

    private FilterConfig config_;

    private String dispatcher_;

    private ServletContext context_;

    public void init(FilterConfig config) throws ServletException {

        config_ = config;
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

        Response response = processRequest(ServletUtils.getPath(httpRequest),
            httpRequest.getMethod().toUpperCase(), dispatcher_, httpRequest
                .getParameterMap());

        if (processResponse(httpRequest, httpResponse, response)) {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    Response processRequest(String path, String method, String dispatcher,
        Map parameterMap) {

        return ((RequestProcessor) getContainer().getComponent(
            RequestProcessor.class)).process(path, method, dispatcher,
            parameterMap);
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
