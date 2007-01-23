package org.seasar.cms.pluggable.hotdeploy;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.seasar.cms.pluggable.PluggableNamingConvention;
import org.seasar.framework.container.ComponentCreator;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.container.impl.S2ContainerImpl;
import org.seasar.framework.container.util.S2ContainerUtil;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ArrayUtil;
import org.seasar.framework.util.ClassTraversal;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.JarFileUtil;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

public class LocalHotdeployS2Container implements ClassHandler {

    private S2Container container_;

    private ClassLoader originalClassLoader_;

    private PluggableHotdeployClassLoader hotdeployClassLoader_;

    private List referenceClassNames_ = new ArrayList();

    private Map strategies_ = new HashMap();

    private Map componentDefCache_ = new HashMap();

    public static final String namingConvention_BINDING = "bindingType=must";

    private PluggableNamingConvention namingConvention_;

    private ComponentCreator[] creators_ = new ComponentCreator[0];

    private HotdeployListener[] listeners_ = new HotdeployListener[0];

    private File classesDirectory_;

    private boolean hotdeployEnabled_ = true;

    private Logger logger_ = Logger.getLogger(getClass());

    public LocalHotdeployS2Container() {
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

    public PluggableNamingConvention getNamingConvention() {
        return namingConvention_;
    }

    public void setNamingConvention(PluggableNamingConvention namingConvention) {
        namingConvention_ = namingConvention;
    }

    public ComponentCreator[] getCreators() {
        return creators_;
    }

    public void setCreators(ComponentCreator[] creators) {
        creators_ = creators;
    }

    public ComponentDef findComponentDef(Object key) {
        if (hotdeployEnabled_) {
            synchronized (this) {
                return findComponentDef0(key);
            }
        } else {
            return getComponentDefFromCache(key);
        }
    }

    protected ComponentDef findComponentDef0(Object key) {
        ComponentDef cd = getComponentDefFromCache(key);
        if (cd != null) {
            return cd;
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    getHotdeployClassLoader());
            if (key instanceof Class) {
                cd = createComponentDef((Class) key);
            } else if (key instanceof String) {
                cd = createComponentDef((String) key);
            } else {
                throw new IllegalArgumentException("key");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        if (cd != null) {
            register(cd);
            S2ContainerUtil.putRegisterLog(cd);
            cd.init();
        }
        return cd;
    }

    protected ComponentDef getComponentDefFromCache(Object key) {
        return (ComponentDef) componentDefCache_.get(key);
    }

    protected ComponentDef createComponentDef(Class componentClass) {
        for (int i = 0; i < creators_.length; ++i) {
            ComponentCreator creator = creators_[i];
            ComponentDef cd = creator.createComponentDef(componentClass);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }

    protected ComponentDef createComponentDef(String componentName) {
        for (int i = 0; i < creators_.length; ++i) {
            ComponentCreator creator = creators_[i];
            ComponentDef cd = creator.createComponentDef(componentName);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }

    public HotdeployClassLoader getHotdeployClassLoader() {
        return hotdeployClassLoader_;
    }

    DistributedHotdeployBehavior getHotdeployBehavior() {
        return (DistributedHotdeployBehavior) S2ContainerBehavior.getProvider();
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
        ComponentDef previousCd = (ComponentDef) componentDefCache_.get(key);
        if (previousCd == null) {
            componentDefCache_.put(key, componentDef);
        } else {
            ComponentDef tmrcd = S2ContainerImpl.createTooManyRegistration(key,
                    previousCd, componentDef);
            componentDefCache_.put(key, tmrcd);
        }
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
            if (path != null) {
                URL url = classLoader.getResource(path);
                if (url != null) {
                    resourceList.add(new ReferenceResource(url, path));
                }
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
                            + "): Please register reference classes to LocalHotdeployS2Container");
        }

        return (ReferenceResource[]) resourceList
                .toArray(new ReferenceResource[0]);
    }

    String getFirstProjectRootPackageName() {
        String[] rootPackageNames = namingConvention_.getRootPackageNames();
        if (rootPackageNames.length > 0) {
            return rootPackageNames[0];
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
            if (namingConvention_.isTargetClassName(className)) {
                ComponentDef cd = createComponentDef(clazz);
                if (cd != null) {
                    register(cd);
                    S2ContainerUtil.putRegisterLog(cd);
                    cd.init();
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public void destroy() {

        componentDefCache_.clear();
        creators_ = new ComponentCreator[0];
        namingConvention_ = null;
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
                    .debug("LocalHotdeployS2Container's start0() method called: classesDirectory="
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

    PluggableHotdeployClassLoader newHotdeployClassLoader(
            ClassLoader originalClassLoader) {
        PluggableHotdeployClassLoader hotdeployClassLoader = new PluggableHotdeployClassLoader(
                originalClassLoader, namingConvention_);
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
                    .debug("LocalHotdeployS2Container's stop0() method called: objectId="
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
            ClassTraversal.forEach(rootDir, LocalHotdeployS2Container.this);
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
            ClassTraversal.forEach(jarFile, LocalHotdeployS2Container.this);
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
            ClassTraversal.forEach(jarFile, LocalHotdeployS2Container.this);
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
