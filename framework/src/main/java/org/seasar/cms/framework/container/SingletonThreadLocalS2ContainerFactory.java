package org.seasar.cms.framework.container;

import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.ExternalContextComponentDefRegister;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.exception.EmptyRuntimeException;

public class SingletonThreadLocalS2ContainerFactory {

    private static String configPath = "app.dicon";

    private static ExternalContext externalContext;

    private static ExternalContextComponentDefRegister externalContextComponentDefRegister;

    private static S2Container container;

    private SingletonThreadLocalS2ContainerFactory() {
    }

    public static String getConfigPath() {
        return configPath;
    }

    public static void setConfigPath(String path) {
        configPath = path;
    }

    public static ExternalContext getExternalContext() {
        return externalContext;
    }

    public static void setExternalContext(ExternalContext extCtx) {
        externalContext = extCtx;
    }

    public static ExternalContextComponentDefRegister getExternalContextComponentDefRegister() {
        return externalContextComponentDefRegister;
    }

    public static void setExternalContextComponentDefRegister(
        ExternalContextComponentDefRegister extCtxComponentDefRegister) {
        externalContextComponentDefRegister = extCtxComponentDefRegister;
    }

    public static void init() {
        container = S2ContainerFactory.create(configPath);
        if (container.getExternalContext() == null) {
            if (externalContext != null) {
                container.setExternalContext(externalContext);
            }
        } else if (container.getExternalContext().getApplication() == null
            && externalContext != null) {
            container.getExternalContext().setApplication(
                externalContext.getApplication());
        }
        if (container.getExternalContextComponentDefRegister() == null
            && externalContextComponentDefRegister != null) {
            container
                .setExternalContextComponentDefRegister(externalContextComponentDefRegister);
        }
        container.init();
    }

    public static void destroy() {
        container.destroy();
        container = null;
    }

    public static S2Container getContainer() {
        if (container == null) {
            throw new EmptyRuntimeException("S2Container");
        }
        return container;
    }

    public static void setContainer(S2Container c) {
        container = c;
    }

    public static boolean hasContainer() {
        return container != null;
    }
}
