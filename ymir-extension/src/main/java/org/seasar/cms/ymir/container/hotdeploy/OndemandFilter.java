package org.seasar.cms.ymir.container.hotdeploy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class OndemandFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        ClassLoader originalClassLoader = Thread.currentThread()
            .getContextClassLoader();
        S2Container container = SingletonS2ContainerFactory.getContainer();
        OndemandUtils.start(container, true);
        Thread.currentThread()
            .setContextClassLoader(container.getClassLoader());
        try {
            chain.doFilter(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            OndemandUtils.stop(container, true);
        }
    }
}