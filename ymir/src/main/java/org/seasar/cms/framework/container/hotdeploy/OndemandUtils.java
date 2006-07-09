package org.seasar.cms.framework.container.hotdeploy;

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

        if (!container.hasComponentDef(LocalOndemandCreatorContainer.class)) {
            return false;
        }
        if (recursive) {
            return true;
        } else {
            ComponentDef componentDef = container
                .getComponentDef(LocalOndemandCreatorContainer.class);
            if (componentDef.getContainer() != container) {
                return false;
            }
            return true;
        }
    }

    protected static LocalOndemandCreatorContainer getLocalOndemandCreatorContainer(
        S2Container container, boolean recursive)
        throws ComponentNotFoundRuntimeException,
        TooManyRegistrationRuntimeException, CyclicReferenceRuntimeException {

        if (recursive) {
            return (LocalOndemandCreatorContainer) container
                .getComponent(LocalOndemandCreatorContainer.class);
        } else {
            ComponentDef[] componentDefs = container
                .findComponentDefs(LocalOndemandCreatorContainer.class);
            for (int i = 0; i < componentDefs.length; i++) {
                if (componentDefs[i].getContainer() == container) {
                    return (LocalOndemandCreatorContainer) componentDefs[i]
                        .getComponent();
                }
            }
            throw new ComponentNotFoundRuntimeException(
                "LocalOndemandCreatorContainer");
        }
    }
}
