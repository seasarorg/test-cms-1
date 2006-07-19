package org.seasar.cms.ymir.container.hotdeploy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.cooldeploy.ConventionNaming;
import org.seasar.framework.container.cooldeploy.DefaultConventionNaming;
import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.container.hotdeploy.HotdeployListener;
import org.seasar.framework.container.hotdeploy.OndemandProject;
import org.seasar.framework.container.hotdeploy.OndemandS2Container;
import org.seasar.framework.container.impl.S2ContainerImpl;
import org.seasar.framework.container.util.S2ContainerUtil;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;
import org.seasar.framework.exception.EmptyRuntimeException;

public class LocalOndemandCreatorContainer implements HotdeployListener,
    OndemandS2Container {

    private S2Container container;

    private ClassLoader originalClassLoader;

    private HotdeployClassLoader hotdeployClassLoader;

    private List projects = new ArrayList();

    private Map componentDefCache = new HashMap();

    public static final String conventionNaming_BINDING = "bindingType=may";

    private ConventionNaming conventionNaming = new DefaultConventionNaming();

    private int counter = 0;

    private HotdeployListener[] hotdeployListeners = new HotdeployListener[0];

    public OndemandProject getProject(int index) {
        return (OndemandProject) projects.get(index);
    }

    public OndemandProject[] getProjects() {
        return (OndemandProject[]) projects
            .toArray(new OndemandProject[projects.size()]);
    }

    public int getProjectSize() {
        return projects.size();
    }

    public void addProject(OndemandProject project) {
        projects.add(project);
    }

    public ConventionNaming getConventionNaming() {
        return conventionNaming;
    }

    public void setConventionNaming(ConventionNaming conventionNaming) {
        this.conventionNaming = conventionNaming;
    }

    public synchronized void start() {
        if (container == null) {
            throw new EmptyRuntimeException("container");
        }
        if (counter++ == 0) {
            originalClassLoader = container.getClassLoader();
            hotdeployClassLoader = newHotdeployClassLoader(originalClassLoader);
            ((S2ContainerImpl) container).setClassLoader(hotdeployClassLoader);
        }
    }

    HotdeployClassLoader newHotdeployClassLoader(ClassLoader originalClassLoader) {
        HotdeployClassLoader hotdeployClassLoader = new HotdeployClassLoader(
            originalClassLoader);
        hotdeployClassLoader.setProjects(getProjects());
        for (int i = 0; i < hotdeployListeners.length; i++) {
            hotdeployClassLoader.addHotdeployListener(hotdeployListeners[i]);
        }
        return hotdeployClassLoader;
    }

    public synchronized void stop() {
        if (--counter == 0) {
            ((S2ContainerImpl) container).setClassLoader(originalClassLoader);
            hotdeployClassLoader = null;
            originalClassLoader = null;
            BeanDescFactory.clear();
            componentDefCache.clear();
        } else if (counter < 0) {
            throw new IllegalStateException("Unbalanced stop() calling");
        }
    }

    public void definedClass(Class clazz) {
        loadComponentDef(clazz);
    }

    public ComponentDef getComponentDef(Class targetClass) {
        return (ComponentDef) componentDefCache.get(targetClass);
    }

    public ComponentDef findComponentDef(Object key) {
        ComponentDef cd = getComponentDefFromCache(key);
        if (cd != null) {
            return cd;
        }
        if (key instanceof Class) {
            return getComponentDef0((Class) key);
        } else if (key instanceof String) {
            return getComponentDef0((String) key);
        } else {
            throw new IllegalArgumentException("key");
        }
    }

    protected ComponentDef getComponentDefFromCache(Object key) {
        return (ComponentDef) componentDefCache.get(key);
    }

    protected void loadComponentDef(Class clazz) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(hotdeployClassLoader);
            for (int i = 0; i < getProjectSize(); ++i) {
                OndemandProject project = getProject(i);
                if (project.loadComponentDef(this, clazz)) {
                    break;
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    protected ComponentDef getComponentDef0(Class clazz) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(hotdeployClassLoader);
            for (int i = 0; i < getProjectSize(); ++i) {
                OndemandProject project = getProject(i);
                ComponentDef cd = project.getComponentDef(this, clazz);
                if (cd != null) {
                    return cd;
                }
            }
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    protected ComponentDef getComponentDef0(String componentName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(hotdeployClassLoader);
            for (int i = 0; i < getProjectSize(); ++i) {
                OndemandProject project = getProject(i);
                try {
                    ComponentDef cd = project.getComponentDef(this, componentName);
                    if (cd != null) {
                        return cd;
                    }
                } catch (ClassNotFoundRuntimeException ignore) {
                }
            }
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public void register(ComponentDef componentDef) {
        componentDef.setContainer(container);
        registerByClass(componentDef);
        registerByName(componentDef);
    }

    protected void registerByClass(ComponentDef componentDef) {
        Class[] classes = S2ContainerUtil.getAssignableClasses(componentDef
            .getComponentClass());
        for (int i = 0; i < classes.length; ++i) {
            registerMap(classes[i], componentDef);
        }
    }

    protected void registerByName(ComponentDef componentDef) {
        String componentName = componentDef.getComponentName();
        if (componentName != null) {
            registerMap(componentName, componentDef);
        }
    }

    protected void registerMap(Object key, ComponentDef componentDef) {
        if (componentDefCache.put(key, componentDef) != null) {
            throw new IllegalStateException(key.toString());
        }
    }

    public void setContainer(S2Container container) {
        this.container = container;
    }

    public ClassLoader getClassLoader() {
        return container.getClassLoader();
    }

    public void setHotdeployListeners(HotdeployListener[] hotdeployListeners) {
        this.hotdeployListeners = hotdeployListeners;
    }
}
