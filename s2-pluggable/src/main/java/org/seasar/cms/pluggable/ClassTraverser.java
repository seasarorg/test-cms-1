package org.seasar.cms.pluggable;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.seasar.framework.container.autoregister.ClassPattern;
import org.seasar.framework.util.ClassTraversal;
import org.seasar.framework.util.JarFileUtil;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

public class ClassTraverser {

    private List classPatterns = new ArrayList();

    private List ignoreClassPatterns = new ArrayList();

    protected List referenceClasses = new ArrayList();

    protected Map strategies = new HashMap();

    protected ClassHandler classHandler;

    public ClassTraverser() {
        strategies.put("file", new FileSystemStrategy());
        strategies.put("jar", new JarFileStrategy());
        strategies.put("zip", new ZipFileStrategy());
    }

    public int getClassPatternSize() {
        return classPatterns.size();
    }

    public ClassPattern getClassPattern(int index) {
        return (ClassPattern) classPatterns.get(index);
    }

    public void addClassPattern(String packageName, String shortClassNames) {

        addClassPattern(new ClassPattern(packageName, shortClassNames));
    }

    public void addClassPattern(ClassPattern classPattern) {
        classPatterns.add(classPattern);
    }

    public void addIgnoreClassPattern(String packageName, String shortClassNames) {

        addIgnoreClassPattern(new ClassPattern(packageName, shortClassNames));
    }

    public void addIgnoreClassPattern(ClassPattern classPattern) {
        ignoreClassPatterns.add(classPattern);
    }

    //    protected boolean hasComponentDef(String name) {
    //        return findComponentDef(name) != null;
    //    }
    //
    //    protected ComponentDef findComponentDef(String name) {
    //        if (name == null) {
    //            return null;
    //        }
    //        S2Container container = getContainer();
    //        for (int i = 0; i < container.getComponentDefSize(); ++i) {
    //            ComponentDef cd = container.getComponentDef(i);
    //            if (name.equals(cd.getComponentName())) {
    //                return cd;
    //            }
    //        }
    //        return null;
    //    }
    //
    protected boolean isIgnore(String packageName, String shortClassName) {
        return isMatched(packageName, shortClassName, ignoreClassPatterns);
    }

    public void addReferenceClass(final Class referenceClass) {
        referenceClasses.add(referenceClass);
    }

    public void addStrategy(final String protocol, final Strategy strategy) {
        strategies.put(protocol, strategy);
    }

    public void setClassHandler(ClassHandler classHandler) {
        if (classHandler == null) {
            this.classHandler = null;
        } else {
            this.classHandler = new FilteredClassHandler(classHandler);
        }
    }

    public void traverse() {
        for (int i = 0; i < referenceClasses.size(); ++i) {
            final Class referenceClass = (Class) referenceClasses.get(i);
            final String baseClassPath = ResourceUtil
                    .getResourcePath(referenceClass);
            final URL url = ResourceUtil.getResource(baseClassPath);
            final Strategy strategy = (Strategy) strategies.get(url
                    .getProtocol());
            strategy.process(referenceClass, url);
        }
    }

    public boolean isMatched(String packageName, String shortClassName) {
        if (isIgnore(packageName, shortClassName)) {
            return false;
        } else if (isMatched(packageName, shortClassName, classPatterns)) {
            return true;
        } else {
            return false;
        }
    }

    boolean isMatched(String packageName, String shortClassName,
            List classPatternList) {

        if (classPatternList.isEmpty()) {
            return false;
        }
        for (Iterator itr = classPatternList.iterator(); itr.hasNext();) {
            ClassPattern cp = (ClassPattern) itr.next();
            if (!cp.isAppliedPackageName(packageName)) {
                continue;
            }
            if (cp.isAppliedShortClassName(shortClassName)) {
                return true;
            }
        }
        return false;
    }

    protected interface Strategy {

        void process(Class referenceClass, URL url);
    }

    protected class FileSystemStrategy implements Strategy {

        public void process(final Class referenceClass, final URL url) {
            final File rootDir = getRootDir(referenceClass, url);
            for (int i = 0; i < getClassPatternSize(); ++i) {
                ClassTraversal.forEach(rootDir, getClassPattern(i)
                        .getPackageName(), classHandler);
            }
        }

        protected File getRootDir(final Class referenceClass, final URL url) {
            final String[] names = referenceClass.getName().split("\\.");
            File path = ResourceUtil.getFile(url);
            for (int i = 0; i < names.length; ++i) {
                path = path.getParentFile();
            }
            return path;
        }
    }

    protected class JarFileStrategy implements Strategy {

        public void process(final Class referenceClass, final URL url) {
            final JarFile jarFile = createJarFile(url);
            ClassTraversal.forEach(jarFile, classHandler);
        }

        protected JarFile createJarFile(final URL url) {
            final String urlString = ResourceUtil.toExternalForm(url);
            final int pos = urlString.lastIndexOf('!');
            final String jarFileName = urlString.substring(
                    "jar:file:".length(), pos);
            return JarFileUtil.create(new File(jarFileName));
        }
    }

    /**
     * WebLogic固有の<code>zip:</code>プロトコルで表現されるURLをサポートするストラテジです。
     */
    protected class ZipFileStrategy implements Strategy {

        public void process(final Class referenceClass, final URL url) {
            final JarFile jarFile = createJarFile(url);
            ClassTraversal.forEach(jarFile, classHandler);
        }

        protected JarFile createJarFile(final URL url) {
            final String urlString = ResourceUtil.toExternalForm(url);
            final int pos = urlString.lastIndexOf('!');
            final String jarFileName = urlString
                    .substring("zip:".length(), pos);
            return JarFileUtil.create(new File(jarFileName));
        }
    }

    class FilteredClassHandler implements ClassHandler {

        private ClassHandler classHandler_;

        public FilteredClassHandler(ClassHandler classHandler) {
            classHandler_ = classHandler;
        }

        public void processClass(String packageName, String shortClassName) {
            if (isIgnore(packageName, shortClassName)) {
                return;
            }

            for (int i = 0; i < getClassPatternSize(); ++i) {
                final ClassPattern cp = getClassPattern(i);
                if (cp.isAppliedPackageName(packageName)
                        && cp.isAppliedShortClassName(shortClassName)) {
                    classHandler_.processClass(packageName, shortClassName);
                }
            }
        }
    }
}
