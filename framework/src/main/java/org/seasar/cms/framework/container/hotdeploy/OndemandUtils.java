package org.seasar.cms.framework.container.hotdeploy;

import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.CyclicReferenceRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.TooManyRegistrationRuntimeException;

public class OndemandUtils {

    private OndemandUtils() {
    }

    public static void start(S2Container container) {

        if (!hasLocalOndemandCreatorContainer(container)) {
            return;
        }
        getLocalOndemandCreatorContainer(container).start();
    }

    public static void stop(S2Container container) {

        if (!hasLocalOndemandCreatorContainer(container)) {
            return;
        }
        getLocalOndemandCreatorContainer(container).stop();
    }

    protected static boolean hasLocalOndemandCreatorContainer(
        S2Container container) {

        return container.hasComponentDef(LocalOndemandCreatorContainer.class);
    }

    protected static LocalOndemandCreatorContainer getLocalOndemandCreatorContainer(
        S2Container container) throws ComponentNotFoundRuntimeException,
        TooManyRegistrationRuntimeException, CyclicReferenceRuntimeException {

        return (LocalOndemandCreatorContainer) container
            .getComponent(LocalOndemandCreatorContainer.class);
    }
}
