package org.seasar.cms.pluggable.hotdeploy;

import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;

/*
 * FIXME s2-container-2.4.0-beta-5以降は不要。
 */
public class TemporaryLocalOndemandS2Container extends LocalOndemandS2Container {
    @Override
    HotdeployClassLoader newHotdeployClassLoader(ClassLoader originalClassLoader) {
        HotdeployClassLoader hotdeployClassLoader = new TemporaryHotdeployClassLoader(
                originalClassLoader);
        hotdeployClassLoader.setProjects(getProjects());
        return hotdeployClassLoader;
    }
}
