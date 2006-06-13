package org.seasar.cms.framework.zpt;

import java.util.Map;

import org.seasar.cms.framework.generator.ClassDesc;
import org.seasar.cms.framework.generator.SourceCreator;
import org.seasar.cms.framework.generator.PropertyDesc;

import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.ZptTemplateContext;

public class AnalyzerContext extends ZptTemplateContext {

    private VariableResolver variableResolver_;

    private SourceCreator sourceCreator_;

    private Map classDescriptorMap_;

    private String pageClassName_;

    private String formActionPageClassName_;

    private String dtoName_;

    private String dtoCollectionName_;

    private String dtoClassName_;

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
            && objs[0] instanceof DummyRepeatObject) {
            dtoName_ = name;
            dtoCollectionName_ = ((DummyRepeatObject) objs[0]).getName();
            dtoClassName_ = sourceCreator_.getDtoPackageName() + "."
                + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            PropertyDesc pd = getPageClassDescriptor().getPropertyDesc(
                dtoCollectionName_);
            pd.addMode(PropertyDesc.ARRAY);
            pd.setDefaultType(dtoClassName_);
        }

        return super.pushRepeatInfo(name, objs);
    }

    public void popRepeatInfo(String name) {

        if (getClassDescriptor(dtoClassName_, false) == null) {
            PropertyDesc pd = getPageClassDescriptor().getPropertyDesc(
                dtoCollectionName_);
            pd.setDefaultType(null);
        }

        dtoName_ = null;
        dtoCollectionName_ = null;
        dtoClassName_ = null;

        super.popRepeatInfo(name);
    }

    public Map getClassDescriptorMap() {

        return classDescriptorMap_;
    }

    public void setClassDescriptorMap(Map classDescriptorMap) {

        classDescriptorMap_ = classDescriptorMap;
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

        return getClassDescriptor(pageClassName_);
    }

    public String getFormActionPageClassName() {

        return formActionPageClassName_;
    }

    public void setFormActionPageClassName(String formActionPageClassName) {

        formActionPageClassName_ = formActionPageClassName;
    }

    public ClassDesc getClassDescriptor(String className) {

        return getClassDescriptor(className, true);
    }

    public ClassDesc getClassDescriptor(String className,
        boolean createIfNotExist) {

        ClassDesc descriptor = (ClassDesc) classDescriptorMap_.get(className);
        if (descriptor == null && createIfNotExist) {
            descriptor = new ClassDesc(className);
            classDescriptorMap_.put(className, descriptor);
        }
        return descriptor;
    }

    public String getDtoName() {

        return dtoName_;
    }

    public String getDtoClassName() {

        return dtoClassName_;
    }
}
