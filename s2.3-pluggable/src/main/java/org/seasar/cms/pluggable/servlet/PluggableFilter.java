package org.seasar.cms.pluggable.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.S2Container;

public class PluggableFilter implements Filter {
    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        S2Container container = SingletonPluggableContainerFactory
                .getRootContainer();
        container.setRequest((HttpServletRequest) request);
        container.setResponse((HttpServletResponse) response);
        try {
            chain.doFilter(request, response);
        } finally {
            container.setRequest(null);
            container.setResponse(null);
        }
    }
}