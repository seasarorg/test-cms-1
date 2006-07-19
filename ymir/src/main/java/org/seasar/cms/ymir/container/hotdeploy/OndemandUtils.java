package org.seasar.cms.ymir.container.hotdeploy;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.CyclicReferenceRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.TooManyRegistrationRuntimeException;

public class OndemandUtils {

    private OndemandUtils() {
    }

    public static void start(S2Container container) {

        start(container, false);
    }

    public static void start(S2Container container, boolean recursive) {

        if (!hasLocalOndemandCreatorContainer(container, recursive)) {
            return;
        }
        getLocalOndemandCreatorContainer(container, recursive).start();
    }

    public static void stop(S2Container container) {

        stop(container, false);
    }

    public static void stop(S2Container container, boolean recursive) {

        if (!hasLocalOndemandCreatorContainer(container, recursive)) {
            return;
        }
        getLocalOndemandCreatorContainer(container, recursive).stop();
    }

    protected static boolean hasLocalOndemandCreatorContainer(
        S2Container container, boolean recursive) {

        if (!container.hasComponentDef(LocalOndemandS2Container.class)) {
            return false;
        }
        if (recursive) {
            return true;
        } else {
            ComponentDef componentDef = container
                .getComponentDef(LocalOndemandS2Container.class);
            if (componentDef.getContainer() != container) {
                return false;
            }
            return true;
        }
    }

    protected static LocalOndemandS2Container getLocalOndemandCreatorContainer(
        S2Container container, boolean recursive)
        throws ComponentNotFoundRuntimeException,
        TooManyRegistrationRuntimeException, CyclicReferenceRuntimeException {

        if (recursive) {
            return (LocalOndemandS2Container) container
                .getComponent(LocalOndemandS2Container.class);
        } else {
            ComponentDef[] componentDefs = container
                .findComponentDefs(LocalOndemandS2Container.class);
            for (int i = 0; i < componentDefs.length; i++) {
                if (componentDefs[i].getContainer() == container) {
                    return (LocalOndemandS2Container) componentDefs[i]
                        .getComponent();
                }
            }
            throw new ComponentNotFoundRuntimeException(
                "LocalOndemandS2Container");
        }
    }
}
