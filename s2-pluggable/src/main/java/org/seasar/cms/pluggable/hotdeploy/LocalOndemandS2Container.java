package org.seasar.cms.pluggable.hotdeploy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.container.hotdeploy.HotdeployListener;
import org.seasar.framework.container.hotdeploy.OndemandProject;
import org.seasar.framework.container.hotdeploy.OndemandS2Container;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.container.impl.S2ContainerImpl;
import org.seasar.framework.container.util.S2ContainerUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ArrayUtil;

public class LocalOndemandS2Container implements HotdeployListener,
        OndemandS2Container {

    private S2Container container_;

    private ClassLoader originalClassLoader_;

    private HotdeployClassLoader hotdeployClassLoader_;

    private List projects_ = new ArrayList();

    private Map componentDefCache_ = Collections.synchronizedMap(new HashMap());

    public static final String namingConvention_BINDING = "bindingType=may";

    private NamingConvention namingConvention_ = new NamingConventionImpl();

    private HotdeployListener[] listeners_ = new HotdeployListener[0];

    private File classesDirectory_;

    private boolean hotdeployEnabled_ = true;

    private Logger logger_ = Logger.getLogger(getClass());

    public OndemandProject getProject(int index) {
        return (OndemandProject) projects_.get(index);
    }

    public OndemandProject[] getProjects() {
        return (OndemandProject[]) projects_.toArray(new OndemandProject[0]);
    }

    public int getProjectSize() {
        return projects_.size();
    }

    public void addProject(OndemandProject project) {
        projects_.add(project);
    }

    public NamingConvention getNamingConvention() {
        return namingConvention_;
    }

    public void setNamingConvention(NamingConvention namingConvention) {
        namingConvention_ = namingConvention;
    }

    public void definedClass(Class clazz) {
        loadComponentDef(clazz);
    }

    public ComponentDef getComponentDef(Class targetClass) {
        return (ComponentDef) componentDefCache_.get(targetClass);
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
        return (ComponentDef) componentDefCache_.get(key);
    }

    protected void loadComponentDef(Class clazz) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    getHotdeployClassLoader());
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
            Thread.currentThread().setContextClassLoader(
                    getHotdeployClassLoader());
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
            Thread.currentThread().setContextClassLoader(
                    getHotdeployClassLoader());
            for (int i = 0; i < getProjectSize(); ++i) {
                OndemandProject project = getProject(i);
                try {
                    ComponentDef cd = project.getComponentDef(this,
                            componentName);
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

    public HotdeployClassLoader getHotdeployClassLoader() {
        return hotdeployClassLoader_;
    }

    DistributedOndemandBehavior getOndemandBehavior() {
        return (DistributedOndemandBehavior) S2ContainerBehavior.getProvider();
    }

    public void register(ComponentDef componentDef) {
        componentDef.setContainer(container_);
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

    protected synchronized void registerMap(Object key,
            ComponentDef componentDef) {
        ComponentDef current = (ComponentDef) componentDefCache_.get(key);
        if (current != null) {
            componentDef = S2ContainerImpl.createTooManyRegistration(key,
                    current, componentDef);
        }
        componentDefCache_.put(key, componentDef);
    }

    public S2Container getContainer() {
        return container_;
    }

    public void setContainer(S2Container container) {
        container_ = container;
    }

    public ClassLoader getOriginalClassLoader() {
        return originalClassLoader_;
    }

    public void init(boolean hotdeployEnabled) {
        if (!hotdeployEnabled) {
            // システムとしてhotdeployが無効なら無効にする。
            // システムとしてhotdeployが有効なら、もともとの状態を保持する。
            hotdeployEnabled_ = false;
        }

        if (!hotdeployEnabled_) {
            start0();
        }
    }

    public void destroy() {

        if (!hotdeployEnabled_) {
            stop0();
        }

        listeners_ = new HotdeployListener[0];
        hotdeployEnabled_ = true;
    }

    public void start() {
        if (hotdeployEnabled_) {
            start0();
        }
    }

    void start0() {
        if (logger_.isDebugEnabled()) {
            logger_
                    .debug("LocalOndemandS2Container's start0() method called: classesDirectory="
                            + classesDirectory_);
        }
        originalClassLoader_ = container_.getClassLoader();
        hotdeployClassLoader_ = newHotdeployClassLoader(originalClassLoader_);
        container_.setClassLoader(hotdeployClassLoader_);

        for (int i = 0; i < listeners_.length; i++) {
            hotdeployClassLoader_.addHotdeployListener(listeners_[i]);
        }

        if (logger_.isDebugEnabled()) {
            logger_.debug("Set HotdeployClassLoader: id="
                    + System.identityHashCode(hotdeployClassLoader_)
                    + ", classDirectory=" + classesDirectory_);
        }
    }

    HotdeployClassLoader newHotdeployClassLoader(ClassLoader originalClassLoader) {
        PluggableHotdeployClassLoader hotdeployClassLoader = new PluggableHotdeployClassLoader(
                originalClassLoader);
        hotdeployClassLoader.setProjects(getProjects());
        if (classesDirectory_ != null) {
            hotdeployClassLoader.setClassesDirectory(classesDirectory_);
        }
        return hotdeployClassLoader;
    }

    public void stop() {
        if (hotdeployEnabled_) {
            stop0();
        }
    }

    void stop0() {
        if (logger_.isDebugEnabled()) {
            logger_
                    .debug("LocalOndemandS2Container's stop0() method called: objectId="
                            + System.identityHashCode(this)
                            + ", classesDirectory=" + classesDirectory_);
        }
        if (logger_.isDebugEnabled()) {
            logger_.debug("Unset HotdeployClassLoader: id="
                    + System.identityHashCode(hotdeployClassLoader_));
        }
        container_.setClassLoader(originalClassLoader_);

        hotdeployClassLoader_ = null;
        originalClassLoader_ = null;

        componentDefCache_.clear();
    }

    public void setClassesDirectory(String classesDirectory) {
        classesDirectory_ = new File(classesDirectory);
    }

    public void setHotdeployDisabled() {
        hotdeployEnabled_ = false;
    }

    public void addHotdeployListener(HotdeployListener listener) {
        listeners_ = (HotdeployListener[]) ArrayUtil.add(listeners_, listener);
    }
}
