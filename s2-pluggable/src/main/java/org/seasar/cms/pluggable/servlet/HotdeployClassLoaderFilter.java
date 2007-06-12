package org.seasar.cms.pluggable.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.seasar.cms.pluggable.hotdeploy.DistributedHotdeployBehavior;
import org.seasar.cms.pluggable.util.PluggableUtils;
import org.seasar.framework.container.impl.S2ContainerBehavior;

public class HotdeployClassLoaderFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        DistributedHotdeployBehavior behavior = (DistributedHotdeployBehavior) S2ContainerBehavior
                .getProvider();
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    PluggableUtils.adjustClassLoader(behavior, classLoader));

            chain.doFilter(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }
}