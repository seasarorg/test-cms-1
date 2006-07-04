package org.seasar.cms.framework.zpt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.TypeDesc;
import org.seasar.cms.framework.creator.impl.ClassDescImpl;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.ZptTemplateContext;

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

        if (objs != null && objs.length == 1 && objs[0] instanceof PropertyDesc) {
            PropertyDesc propertyDesc = (PropertyDesc) objs[0];
            TypeDesc typeDesc = propertyDesc.getTypeDesc();
            typeDesc.setArray(true);
            typeDesc.setClassDesc(getTemporaryClassDesc(name));
        }

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

    public ClassDesc getTemporaryClassDesc(String className) {

        int dot = className.lastIndexOf('.');
        if (dot < 0) {
            className = toClassName(className);
        }
        ClassDesc classDesc = (ClassDesc) temporaryClassDescMap_.get(className);
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

    public ClassDesc getPageClassDescriptor() {

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
            ClassDesc classDesc = (ClassDesc) entry.getValue();
            if (isEmptyDto(classDesc)) {
                // 中身のないDTOは無視する。
                continue;
            } else {
                // 中身のないDTOを型にもつプロパティについては、
                // 型をデフォルトクラスに差し替える。（DTOかもしれないと考えて
                // 解析を進めたが結局DTOであることが確定しなかったので。）
                PropertyDesc[] pds = classDesc.getPropertyDescs();
                for (int i = 0; i < pds.length; i++) {
                    if (isEmptyDto(pds[i].getTypeDesc().getClassDesc())) {
                        pds[i].getTypeDesc().setClassDesc(
                            TypeDesc.DEFAULT_CLASSDESC);
                    }
                }
            }
            classDesc.merge((ClassDesc) classDescMap_.get(name), true);
            classDescMap_.put(name, classDesc);
        }
    }

    boolean isEmptyDto(ClassDesc classDesc) {

        return (ClassDesc.KIND_DTO.equals(classDesc.getKind()) && classDesc
            .isEmpty());
    }

    public String getDtoClassName(String baseName) {

        return sourceCreator_.getDtoPackageName() + "." + capFirst(baseName)
            + ClassDesc.KIND_DTO;
    }

    String capFirst(String string) {

        if (string == null || string.length() == 0) {
            return string;
        } else {
            return Character.toUpperCase(string.charAt(0))
                + string.substring(1);
        }
    }

    public PropertyDesc getPropertyDesc(ClassDesc classDesc, String name,
        char delimChar, int mode) {

        int delim = name.indexOf(delimChar);
        if (delim < 0) {
            return classDesc.addProperty(name, mode);
        } else {
            String baseName = name.substring(0, delim);
            PropertyDesc propertyDesc = classDesc.addProperty(baseName,
                PropertyDesc.READ);
            ClassDesc typeClassDesc = prepareTypeClassDesc(propertyDesc);
            if (typeClassDesc != null) {
                return getPropertyDesc(typeClassDesc,
                    name.substring(delim + 1), delimChar, mode);
            } else {
                return propertyDesc;
            }
        }
    }

    public ClassDesc prepareTypeClassDesc(PropertyDesc propertyDesc) {

        ClassDesc cd = propertyDesc.getTypeDesc().getClassDesc();
        ClassDesc returned = null;
        if (cd == TypeDesc.DEFAULT_CLASSDESC) {
            returned = getTemporaryClassDesc(propertyDesc.getName());
            propertyDesc.getTypeDesc().setClassDesc(returned);
        } else if (cd instanceof ClassDescImpl) {
            returned = cd;
        }
        return returned;
    }
}
