package org.seasar.cms.framework.container;

import org.seasar.framework.container.ClassUnmatchRuntimeException;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.ContainerNotRegisteredRuntimeException;
import org.seasar.framework.container.CyclicReferenceRuntimeException;
import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.ExternalContextComponentDefRegister;
import org.seasar.framework.container.MetaDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.TooManyRegistrationRuntimeException;

public class ThreadLocalS2Container implements S2Container {

    private S2Container container_;

    public ThreadLocalS2Container(S2Container container) {
        container_ = container;
    }

    public void addMetaDef(MetaDef metaDef) {
        container_.addMetaDef(metaDef);
    }

    public void addParent(S2Container parent) {
        container_.addParent(parent);
    }

    public void destroy() {
        container_.destroy();
    }

    public ComponentDef[] findComponentDefs(Object componentKey) {
        return container_.findComponentDefs(componentKey);
    }

    public Object[] findComponents(Object componentKey)
        throws CyclicReferenceRuntimeException {
        return container_.findComponents(componentKey);
    }

    public S2Container getChild(int index) {
        return container_.getChild(index);
    }

    public int getChildSize() {
        return container_.getChildSize();
    }

    public ClassLoader getClassLoader() {
        return container_.getClassLoader();
    }

    public Object getComponent(Object componentKey)
        throws ComponentNotFoundRuntimeException,
        TooManyRegistrationRuntimeException, CyclicReferenceRuntimeException {
        return container_.getComponent(componentKey);
    }

    public ComponentDef getComponentDef(int index) {
        return container_.getComponentDef(index);
    }

    public ComponentDef getComponentDef(Object componentKey)
        throws ComponentNotFoundRuntimeException {
        return container_.getComponentDef(componentKey);
    }

    public int getComponentDefSize() {
        return container_.getComponentDefSize();
    }

    public S2Container getDescendant(String path)
        throws ContainerNotRegisteredRuntimeException {
        return container_.getDescendant(path);
    }

    public ExternalContext getExternalContext() {
        return container_.getExternalContext();
    }

    public ExternalContextComponentDefRegister getExternalContextComponentDefRegister() {
        return container_.getExternalContextComponentDefRegister();
    }

    public MetaDef getMetaDef(int index) {
        return container_.getMetaDef(index);
    }

    public MetaDef getMetaDef(String name) {
        return container_.getMetaDef(name);
    }

    public MetaDef[] getMetaDefs(String name) {
        return container_.getMetaDefs(name);
    }

    public int getMetaDefSize() {
        return container_.getMetaDefSize();
    }

    public String getNamespace() {
        return container_.getNamespace();
    }

    public S2Container getParent(int index) {
        return container_.getParent(index);
    }

    public int getParentSize() {
        return container_.getParentSize();
    }

    public String getPath() {
        return container_.getPath();
    }

    public S2Container getRoot() {
        return container_.getRoot();
    }

    public boolean hasComponentDef(Object componentKey) {
        return container_.hasComponentDef(componentKey);
    }

    public boolean hasDescendant(String path) {
        return container_.hasDescendant(path);
    }

    public void include(S2Container child) {
        container_.include(child);
    }

    public void init() {
        container_.init();
    }

    public void injectDependency(Object outerComponent, Class componentClass)
        throws ClassUnmatchRuntimeException {
        container_.injectDependency(outerComponent, componentClass);
    }

    public void injectDependency(Object outerComponent, String componentName)
        throws ClassUnmatchRuntimeException {
        container_.injectDependency(outerComponent, componentName);
    }

    public void injectDependency(Object outerComponent)
        throws ClassUnmatchRuntimeException {
        container_.injectDependency(outerComponent);
    }

    public boolean isHotswapMode() {
        return container_.isHotswapMode();
    }

    public void register(Class componentClass, String componentName) {
        container_.register(componentClass, componentName);
    }

    public void register(Class componentClass) {
        container_.register(componentClass);
    }

    public void register(ComponentDef componentDef) {
        container_.register(componentDef);
    }

    public void register(Object component, String componentName) {
        container_.register(component, componentName);
    }

    public void register(Object component) {
        container_.register(component);
    }

    public void registerDescendant(S2Container descendant) {
        container_.registerDescendant(descendant);
    }

    public void registerMap(Object key, ComponentDef componentDef,
        S2Container container) {
        container_.registerMap(key, componentDef, container);
    }

    public void setExternalContext(ExternalContext externalContext) {
        container_.setExternalContext(externalContext);
    }

    public void setExternalContextComponentDefRegister(
        ExternalContextComponentDefRegister externalContextComponentDefRegister) {
        container_
            .setExternalContextComponentDefRegister(externalContextComponentDefRegister);
    }

    public void setHotswapMode(boolean hotswapMode) {
        container_.setHotswapMode(hotswapMode);
    }

    public void setNamespace(String namespace) {
        container_.setNamespace(namespace);
    }

    public void setPath(String path) {
        container_.setPath(path);
    }

    public void setRoot(S2Container root) {
        container_.setRoot(root);
    }
}
