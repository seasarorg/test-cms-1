package org.seasar.cms.pluggable.hotdeploy;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

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
import org.seasar.framework.util.ClassTraversal;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.JarFileUtil;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

public class LocalOndemandS2Container implements HotdeployListener,
        ClassHandler, OndemandS2Container {

    private S2Container container_;

    private ClassLoader originalClassLoader_;

    private HotdeployClassLoader hotdeployClassLoader_;

    private List projects_ = new ArrayList();

    private List referenceClassNames_ = new ArrayList();

    private Map strategies_ = new HashMap();

    private Map componentDefCache_ = new HashMap();

    public static final String namingConvention_BINDING = "bindingType=may";

    private NamingConvention namingConvention_ = new NamingConventionImpl();

    private HotdeployListener[] listeners_ = new HotdeployListener[0];

    private File classesDirectory_;

    private boolean hotdeployEnabled_ = true;

    private Logger logger_ = Logger.getLogger(getClass());

    public LocalOndemandS2Container() {
        addStrategy("file", new FileSystemStrategy());
        addStrategy("jar", new JarFileStrategy());
        addStrategy("zip", new ZipFileStrategy());
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

    public String getReferenceClassName(int index) {
        return (String) referenceClassNames_.get(index);
    }

    public String[] getReferenceClassNames() {
        return (String[]) referenceClassNames_.toArray(new String[0]);
    }

    public int getReferenceClassNameSize() {
        return referenceClassNames_.size();
    }

    public void addReferenceClassName(String referenceClassName) {
        referenceClassNames_.add(referenceClassName);
    }

    public Map getStrategies() {
        return strategies_;
    }

    protected Strategy getStrategy(String protocol) {
        return (Strategy) strategies_.get(protocol);
    }

    protected void addStrategy(String protocol, Strategy strategy) {
        strategies_.put(protocol, strategy);
    }

    public NamingConvention getNamingConvention() {
        return namingConvention_;
    }

    public void setNamingConvention(NamingConvention namingConvention) {
        namingConvention_ = namingConvention;
    }

    public synchronized void definedClass(Class clazz) {
        loadComponentDef(clazz);
    }

    public ComponentDef getComponentDef(Class targetClass) {
        if (hotdeployEnabled_) {
            synchronized (this) {
                return getComponentDefFromCache(targetClass);
            }
        } else {
            return getComponentDefFromCache(targetClass);
        }
    }

    public ComponentDef findComponentDef(Object key) {
        if (hotdeployEnabled_) {
            synchronized (this) {
                return findComponentDef0(key);
            }
        } else {
            return findComponentDef0(key);
        }
    }

    protected ComponentDef findComponentDef0(Object key) {
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

    public synchronized void register(ComponentDef componentDef) {
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

        // hotdeployがenableの時でも、reference resourceが登録されていないことをチェック
        // するためにgetReferenceResources()を呼び出している。こうすれば、開発環境で動いて
        // いたものがいきなり本番環境でエラーになる心配がなくなる。
        ReferenceResource[] resources = getReferenceResources();
        if (!hotdeployEnabled_) {
            registerComponents(resources);
        }
    }

    void registerComponents(ReferenceResource[] resources) {

        for (int i = 0; i < resources.length; i++) {
            Strategy strategy = getStrategy(resources[i].getURL().getProtocol());
            strategy.registerAll(resources[i]);
        }
    }

    ReferenceResource[] getReferenceResources() {

        ClassLoader classLoader = container_.getClassLoader();
        List resourceList = new ArrayList();
        if (referenceClassNames_.size() == 0) {
            // リファレンスクラス名が無指定の場合はコンテナに対応するdiconファイルを
            // 読み込んだクラスローダを基準とする。
            String path = container_.getPath();
            URL url = classLoader.getResource(path);
            if (url != null) {
                resourceList.add(new ReferenceResource(url, path));
            }
        } else {
            for (Iterator itr = referenceClassNames_.iterator(); itr.hasNext();) {
                String referenceClassName = (String) itr.next();
                String resourceName = referenceClassName.replace('.', '/')
                        .concat(".class");
                URL url = classLoader.getResource(resourceName);
                if (url == null) {
                    throw new RuntimeException("Project ("
                            + getFirstProjectRootPackageName()
                            + "): Can't find class resource for: "
                            + referenceClassName + ": from classLoader: "
                            + classLoader);
                }
                resourceList.add(new ReferenceResource(url, resourceName));
            }
        }
        if (resourceList.size() == 0) {
            throw new RuntimeException(
                    "Project ("
                            + getFirstProjectRootPackageName()
                            + "): Please register reference classes to LocalOndemandS2Container");
        }

        return (ReferenceResource[]) resourceList
                .toArray(new ReferenceResource[0]);
    }

    String getFirstProjectRootPackageName() {
        if (projects_.size() > 0) {
            return ((OndemandProject) projects_.get(0)).getRootPackageName();
        } else {
            return "(Unknown)";
        }
    }

    public void processClass(String packageName, String shortClassName) {
        String className = ClassUtil.concatName(packageName, shortClassName);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    container_.getClassLoader());

            Class clazz = ClassUtil.forName(className);

            for (int i = 0; i < getProjectSize(); ++i) {
                OndemandProject project = getProject(i);
                int m = project.matchClassName(className);
                if (m == OndemandProject.IGNORE) {
                    break;
                } else if (m == OndemandProject.UNMATCH) {
                    continue;
                }
                if (project.loadComponentDef(this, clazz)) {
                    break;
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public void destroy() {

        componentDefCache_.clear();
        projects_.clear();
        referenceClassNames_.clear();
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

        synchronized (this) {
            componentDefCache_.clear();
        }
    }

    protected interface Strategy {

        void registerAll(ReferenceResource resource);
    }

    protected class FileSystemStrategy implements Strategy {

        public void registerAll(ReferenceResource resource) {
            File rootDir = getRootDir(resource);
            ClassTraversal.forEach(rootDir, LocalOndemandS2Container.this);
        }

        protected File getRootDir(ReferenceResource resource) {
            File file = ResourceUtil.getFile(resource.getURL());
            String[] names = StringUtil.split(resource.getResourceName(), "/");
            for (int i = 0; i < names.length; ++i) {
                file = file.getParentFile();
            }
            return file;
        }
    }

    protected class JarFileStrategy implements Strategy {

        public void registerAll(ReferenceResource resource) {
            JarFile jarFile = createJarFile(resource.getURL());
            ClassTraversal.forEach(jarFile, LocalOndemandS2Container.this);
        }

        protected JarFile createJarFile(URL url) {
            String urlString = ResourceUtil.toExternalForm(url);
            int pos = urlString.lastIndexOf('!');
            String jarFileName = urlString.substring("jar:file:".length(), pos);
            return JarFileUtil.create(new File(jarFileName));
        }
    }

    /**
     * WebLogic固有の<code>zip:</code>プロトコルで表現されるURLをサポートするストラテジです。
     */
    protected class ZipFileStrategy implements Strategy {

        public void registerAll(ReferenceResource resource) {
            final JarFile jarFile = createJarFile(resource.getURL());
            ClassTraversal.forEach(jarFile, LocalOndemandS2Container.this);
        }

        protected JarFile createJarFile(URL url) {
            final String urlString = ResourceUtil.toExternalForm(url);
            final int pos = urlString.lastIndexOf('!');
            final String jarFileName = urlString
                    .substring("zip:".length(), pos);
            return JarFileUtil.create(new File(jarFileName));
        }
    }

    protected static class ReferenceResource {

        private URL url_;

        private String resourceName_;

        public ReferenceResource(URL url, String resourceName) {
            super();

            url_ = url;
            resourceName_ = resourceName;
        }

        public String getResourceName() {
            return resourceName_;
        }

        public URL getURL() {
            return url_;
        }
    }
}
