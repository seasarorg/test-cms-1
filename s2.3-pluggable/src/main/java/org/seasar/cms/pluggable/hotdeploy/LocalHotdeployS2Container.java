package org.seasar.cms.pluggable.hotdeploy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.seasar.cms.pluggable.PluggableNamingConvention;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;

public class LocalHotdeployS2Container {
    private S2Container container_;

    private List<String> referenceClassNames_ = new ArrayList<String>();

    private PluggableNamingConvention namingConvention_;

    public void setClassesDirectory(String classesDirectory) {
    }

    public void setHotdeployDisabled() {
    }

    public void addHotdeployListener(HotdeployListener listener) {
    }

    public String getReferenceClassName(int index) {
        return referenceClassNames_.get(index);
    }

    public String[] getReferenceClassNames() {
        return referenceClassNames_.toArray(new String[0]);
    }

    public int getReferenceClassNameSize() {
        return referenceClassNames_.size();
    }

    public void addReferenceClassName(String referenceClassName) {
        referenceClassNames_.add(referenceClassName);
    }

    public Map<String, Strategy> getStrategies() {
        return Collections.emptyMap();
    }

    public PluggableNamingConvention getNamingConvention() {
        return namingConvention_;
    }

    public void setNamingConvention(PluggableNamingConvention namingConvention) {
        namingConvention_ = namingConvention;
    }

    public ComponentDef findComponentDef(Object key) {
        return null;
    }

    public synchronized void register(ComponentDef componentDef) {
    }

    public S2Container getContainer() {
        return container_;
    }

    public void setContainer(S2Container container) {
        container_ = container;
    }

    public ClassLoader getOriginalClassLoader() {
        return null;
    }

    public void init(boolean hotdeployEnabled) {
    }

    public void destroy() {
    }

    public void start() {
    }

    public void stop() {
    }

    protected interface Strategy {
    }
}
