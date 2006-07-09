package org.seasar.cms.ymir.container;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class ThreadLocalS2ContainerUtils {

    private ThreadLocalS2ContainerUtils() {
    }

    public static void register(Object component) {

        register(SingletonS2ContainerFactory.getContainer(), component);
    }

    public static void register(S2Container container, Object component) {

        if (component == null) {
            throw new IllegalArgumentException("component can't be null");
        }

        synchronized (container) {
            Class componentClass = component.getClass();
            ComponentDef[] componentDefs = container
                .findComponentDefs(componentClass);
            if (componentDefs.length == 0) {
                ThreadLocalComponentDef componentDef = new ThreadLocalComponentDef(
                    componentClass);
                container.register(componentDef);
            } else if (componentDefs.length == 1) {
                try {
                    ((ThreadLocalComponentDef) componentDefs[0])
                        .setComponent(component);
                } catch (ClassCastException ex) {
                    throw (ClassCastException) new ClassCastException(
                        "*.dicon may have an entry for '" + componentClass
                            + "' class.").initCause(ex);
                }
            } else {
                throw new RuntimeException(
                    "*.dicon may have multiple entries for '" + componentClass
                        + "' class.");
            }
        }
    }

    public static void deregister(Object component) {

        deregister(SingletonS2ContainerFactory.getContainer(), component);
    }

    public static void deregister(S2Container container, Object component) {

        if (component == null) {
            throw new IllegalArgumentException("component can't be null");
        }

        synchronized (container) {
            Class componentClass = component.getClass();
            ComponentDef[] componentDefs = container
                .findComponentDefs(componentClass);
            if (componentDefs.length == 0) {
                ;
            } else if (componentDefs.length == 1) {
                try {
                    ((ThreadLocalComponentDef) componentDefs[0])
                        .setComponent(null);
                } catch (ClassCastException ex) {
                    throw (ClassCastException) new ClassCastException(
                        "*.dicon may have an entry for '" + componentClass
                            + "' class.").initCause(ex);
                }
            } else {
                throw new RuntimeException(
                    "*.dicon may have multiple entries for '" + componentClass
                        + "' class.");
            }
        }
    }
}
