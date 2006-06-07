package org.seasar.cms.framework.container.hotdeploy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class OndemandFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        OndemandUtils.start(SingletonS2ContainerFactory.getContainer());
        try {
            chain.doFilter(request, response);
        } finally {
            OndemandUtils.stop(SingletonS2ContainerFactory.getContainer());
        }
    }
}