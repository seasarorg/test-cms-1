package org.seasar.cms.pluggable.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.cms.pluggable.classloader.CompositeClassLoader;
import org.seasar.cms.pluggable.hotdeploy.DistributedHotdeployBehavior;
import org.seasar.cms.pluggable.hotdeploy.LocalHotdeployS2Container;
import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.container.impl.S2ContainerBehavior;

public class PluggableFilter implements Filter {

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
        behavior.start();
        try {
            Thread.currentThread().setContextClassLoader(
                    adjustClassLoader(behavior, classLoader));

            ExternalContext externalContext = SingletonPluggableContainerFactory
                    .getRootContainer().getExternalContext();
            externalContext.setRequest(request);
            externalContext.setResponse(response);
            try {
                chain.doFilter(request, response);
            } finally {
                externalContext.setRequest(null);
                externalContext.setResponse(null);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
            behavior.stop();
        }
    }

    ClassLoader adjustClassLoader(DistributedHotdeployBehavior behavior,
            ClassLoader parent) {

        LocalHotdeployS2Container[] containers = behavior
                .getLocalHotdeployS2Containers();
        List classLoaderList = new ArrayList();
        for (int i = 0; i < containers.length; i++) {
            HotdeployClassLoader classLoader = containers[i]
                    .getHotdeployClassLoader();
            if (classLoader != null) {
                classLoaderList.add(classLoader);
            }
        }
        if (classLoaderList.size() > 0) {
            return new CompositeClassLoader((ClassLoader[]) classLoaderList
                    .toArray(new ClassLoader[0]), parent);
        } else {
            return parent;
        }
    }
}