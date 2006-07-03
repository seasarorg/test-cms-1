package org.seasar.cms.framework.zpt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.ZptTemplateContext;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.TypeDesc;
import org.seasar.cms.framework.creator.impl.ClassDescImpl;
import org.seasar.cms.framework.creator.impl.SimpleClassDesc;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

public class AnalyzerContext extends ZptTemplateContext {

    private VariableResolver variableResolver_;

    private SourceCreator sourceCreator_;

    private String method_;

    private Map classDescMap_;

    private Map temporaryClassDescMap_ = new LinkedHashMap();

    private String pageClassName_;

    private String formActionPageClassName_;

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

        //TODO 書こう。
        //        if (objs != null && objs.length == 1
        //            && objs instanceof PropertyHolder.Property[]) {
        //            PropertyHolder.Property property = (PropertyHolder.Property) objs[0];
        //            PropertyHolder propertyHolder = getPropertyHolder(name);
        //            property.setType(propertyHolder);
        //            property.setArray(true);
        //        }

        return super.pushRepeatInfo(name, objs);
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

    public ClassDescImpl getTemporaryClassDesc(String className) {

        int dot = className.lastIndexOf('.');
        if (dot < 0) {
            className = toClassName(className);
        }
        ClassDescImpl classDesc = (ClassDescImpl) temporaryClassDescMap_
            .get(className);
        if (classDesc == null) {
            classDesc = new ClassDescImpl(className);
            temporaryClassDescMap_.put(className, classDesc);
        }
        return classDesc;
    }

    public String toClassName(String componentName) {

        if (DefaultRequestProcessor.ATTR_PAGE.equals(componentName)) {
            return getPageClassName();
        } else {
            String className = sourceCreator_.getClassName(componentName);
            if (className == null) {
                className = getDtoClassName(componentName);
            }
            return className;
        }
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

    public ClassDescImpl getPageClassDescriptor() {

        return getTemporaryClassDesc(pageClassName_);
    }

    public String getFormActionPageClassName() {

        return formActionPageClassName_;
    }

    public void setFormActionPageClassName(String formActionPageClassName) {

        formActionPageClassName_ = formActionPageClassName;
    }

    public void close() {

        for (Iterator itr = temporaryClassDescMap_.entrySet().iterator(); itr
            .hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            String name = (String) entry.getKey();
            ClassDescImpl classDesc = (ClassDescImpl) entry.getValue();
            if (isEmptyDto(classDesc)) {
                // 中身のないDTOは無視する。
                continue;
            } else {
                // 中身のないDTOを型にもつプロパティについては、
                // 型をStringに差し替える。（DTOかもしれないと考えて
                // 解析を進めたが結局DTOであることが確定しなかったので。）
                PropertyDesc[] pds = classDesc.getPropertyDescs();
                for (int i = 0; i < pds.length; i++) {
                    if (isEmptyDto(pds[i].getTypeDesc().getClassDesc())) {
                        pds[i].getTypeDesc().setClassDesc(
                            new SimpleClassDesc(String.class.getName()));
                    }
                }
            }
            classDesc.merge((ClassDescImpl) classDescMap_.get(name), true);
            classDescMap_.put(name, classDesc);
        }
    }

    boolean isEmptyDto(ClassDesc classDesc) {

        if (!(classDesc instanceof ClassDescImpl)) {
            return true;
        }
        ClassDescImpl impl = (ClassDescImpl) classDesc;
        if (!ClassDesc.KIND_DTO.equals(impl.getKind())) {
            return true;
        }
        if (!impl.isEmpty()) {
            return true;
        }
        return false;
    }

    public String getDtoClassName(String baseName) {

        return sourceCreator_.getDtoPackageName() + "." + baseName
            + ClassDesc.KIND_DTO;
    }

    public PropertyDesc getPropertyDesc(ClassDescImpl classDesc, String name,
        char delimChar, int mode) {

        int delim = name.indexOf(delimChar);
        if (delim < 0) {
            return classDesc.addProperty(name, mode);
        } else {
            String baseName = name.substring(0, delim);
            String dtoClassName = getDtoClassName(baseName);
            PropertyDesc propertyDesc = classDesc.addProperty(baseName,
                PropertyDesc.READ);
            TypeDesc typeDesc = propertyDesc.getTypeDesc();
            String typeClassName = typeDesc.getClassDesc().getName();
            ClassDescImpl typeClassDesc = null;
            if (typeClassName.equals(String.class.getName())) {
                typeClassDesc = getTemporaryClassDesc(dtoClassName);
                typeDesc.setClassDesc(typeClassDesc);
            } else if (typeClassName.equals(dtoClassName)) {
                typeClassDesc = (ClassDescImpl) typeDesc.getClassDesc();
            }
            if (typeClassDesc != null) {
                return getPropertyDesc(typeClassDesc,
                    name.substring(delim + 1), delimChar, mode);
            } else {
                return propertyDesc;
            }
        }
    }
}
