package org.seasar.cms.framework.generator;

import org.seasar.cms.framework.Response;
import org.seasar.framework.container.S2Container;

public interface PageClassUpdater {

    Response update(S2Container container, String componentName, String path);
}
