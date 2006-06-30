package org.seasar.cms.framework.zpt;

import java.util.HashMap;
import java.util.Map;

import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.ZptTemplateContext;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyHolder;
import org.seasar.cms.framework.creator.SourceCreator;

public class AnalyzerContext extends ZptTemplateContext {

    private VariableResolver variableResolver_;

    private SourceCreator sourceCreator_;

    private String method_;

    private Map classDescMap_;

    private String pageClassName_;

    private String formActionPageClassName_;

    private Map propertyHolderMap_ = new HashMap();

    public AnalyzerContext() {

        variableResolver_ = new AnalyzerVariableResolver(super
            .getVariableResolver());
    }

    public VariableResolver getVariableResolver() {
        return variableResolver_;
    }

    public void defineVariable(int scope, String name, Object value) {

        super.defineVariable(scope, name, value);
    }

    public RepeatInfo pushRepeatInfo(String name, Object[] objs) {

        if (objs != null && objs.length == 1
            && objs instanceof PropertyHolder.Property[]) {
            PropertyHolder.Property property = (PropertyHolder.Property) objs[0];
            PropertyHolder propertyHolder = getPropertyHolder(name);
            property.setType(propertyHolder);
            property.setArray(true);
        }

        return super.pushRepeatInfo(name, objs);
    }

    public PropertyHolder getPropertyHolder(String name) {
        return getPropertyHolder(name, true);
    }

    public PropertyHolder getPropertyHolder(String name, boolean create) {

        PropertyHolder propertyHolder = (PropertyHolder) propertyHolderMap_
            .get(name);
        if (propertyHolder == null && create) {
            propertyHolder = new PropertyHolder(name);
            propertyHolderMap_.put(name, propertyHolder);
        }
        return propertyHolder;
    }

    public String getMethod() {

        return method_;
    }

    public void setMethod(String method) {

        method_ = method;
    }

    public Map getClassDescMap() {

        return classDescMap_;
    }

    public void setClassDescMap(Map classDescriptorMap) {

        classDescMap_ = classDescriptorMap;
    }

    public String getPageClassName() {

        return pageClassName_;
    }

    public void setPageClassName(String pageClassName) {

        pageClassName_ = pageClassName;
    }

    public SourceCreator getSourceCreator() {

        return sourceCreator_;
    }

    public void setSourceCreator(SourceCreator sourceCreator) {

        sourceCreator_ = sourceCreator;
    }

    public ClassDesc getPageClassDescriptor() {

        return getClassDesc(pageClassName_);
    }

    public String getFormActionPageClassName() {

        return formActionPageClassName_;
    }

    public void setFormActionPageClassName(String formActionPageClassName) {

        formActionPageClassName_ = formActionPageClassName;
    }

    public ClassDesc getClassDesc(String className) {

        return getClassDesc(className, true);
    }

    public ClassDesc getClassDesc(String className, boolean createIfNotExist) {

        ClassDesc descriptor = (ClassDesc) classDescMap_.get(className);
        if (descriptor == null && createIfNotExist) {
            descriptor = new ClassDesc(className);
            classDescMap_.put(className, descriptor);
        }
        return descriptor;
    }
}
