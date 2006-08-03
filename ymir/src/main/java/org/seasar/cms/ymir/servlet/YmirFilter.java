package org.seasar.cms.ymir.servlet;

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

import org.seasar.cms.ymir.MultipartServletRequest;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.Ymir;
import org.seasar.cms.ymir.util.ServletUtils;

public class YmirFilter implements Filter {

    private String dispatcher_;

    private ServletContext context_;

    private Ymir ymir_;

    public void init(FilterConfig config) throws ServletException {

        context_ = config.getServletContext();
        ymir_ = (Ymir) context_.getAttribute(YmirListener.YMIR_KEY);

        String dispatcher = config.getInitParameter("dispatcher");
        if (dispatcher == null) {
            throw new ServletException(
                "Init-param 'dispatcher' must be specified");
        }
        dispatcher_ = dispatcher.toUpperCase();
    }

    public void destroy() {

        context_ = null;
        ymir_ = null;
        dispatcher_ = null;
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
            Response response = ymir_.processRequest(ServletUtils
                .getContextPath(httpRequest),
                ServletUtils.getPath(httpRequest), httpRequest.getMethod()
                    .toUpperCase(), dispatcher_, httpRequest.getParameterMap(),
                fileParameterMap);

            if (ymir_.processResponse(context_, httpRequest, httpResponse,
                response)) {
                chain.doFilter(httpRequest, httpResponse);
            }
        } catch (PageNotFoundException ex) {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
