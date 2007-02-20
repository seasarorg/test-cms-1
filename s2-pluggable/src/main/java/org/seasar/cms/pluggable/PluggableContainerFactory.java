package org.seasar.cms.pluggable;

import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.ExternalContextComponentDefRegister;
import org.seasar.framework.container.S2Container;

public interface PluggableContainerFactory {

    String COMPONENTS_DICON = "META-INF/s2container/components.dicon";

    String ROOT_DICON = "root.dicon";

    String GLOBAL_DICON = "global.dicon";

    String META_EXPAND = "expand";

    String getConfigPath();

    void setConfigPath(String path);

    ExternalContext getExternalContext();

    void setExternalContext(ExternalContext extCtx);

    ExternalContextComponentDefRegister getExternalContextComponentDefRegister();

    void setExternalContextComponentDefRegister(
            ExternalContextComponentDefRegister extCtxComponentDefRegister);

    Object getApplication();

    void setApplication(Object application);

    void prepareForContainer();

    S2Container integrate(String configPath, S2Container[] dependencies);

    S2Container integrate(String configPath, ClassLoader classLoader,
            S2Container[] dependencies);

    S2Container processExpanding(S2Container container);

    void init();

    void destroy();

    S2Container getRootContainer();

    void setRootContainer(S2Container container);

    boolean hasRootContainer();
}
