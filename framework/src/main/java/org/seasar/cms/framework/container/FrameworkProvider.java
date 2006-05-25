package org.seasar.cms.framework.container;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class FrameworkProvider extends S2ContainerFactory.DefaultProvider {

    protected S2Container build(String path, ClassLoader classLoader) {

        return new ThreadLocalS2Container(super.build(path, classLoader));
    }
}
