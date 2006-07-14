package org.seasar.cms.ymir.zpt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.ZptTemplateContext;

import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.PropertyDesc;
import org.seasar.cms.ymir.creator.SourceCreator;
import org.seasar.cms.ymir.creator.TypeDesc;
import org.seasar.cms.ymir.creator.impl.ClassDescImpl;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;

public class AnalyzerContext extends ZptTemplateContext {

    private static final String MULTIPLE_SUFFIX = "s";

    private static final char STR_ARRAY_LPAREN = '[';

    private static final char CHAR_ARRAY_RPAREN = ']';

    private VariableResolver variableResolver_;

    private SourceCreator sourceCreator_;

    private String method_;

    private Map<String, ClassDesc> classDescMap_;

    private Map<String, ClassDesc> temporaryClassDescMap_ = new LinkedHashMap<String, ClassDesc>();

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

        if (objs != null && objs.length == 1 && objs[0] instanceof DescWrapper) {
            DescWrapper wrapper = (DescWrapper) objs[0];
            PropertyDesc propertyDesc = wrapper.getPropertyDesc();
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

    public void setClassDescMap(Map<String, ClassDesc> classDescMap) {

        classDescMap_ = classDescMap;
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
        int mode) {

        int dot = name.indexOf('.');
        if (dot < 0) {
            return getSinglePropertyDesc(classDesc, name, mode);
        } else {
            String baseName = name.substring(0, dot);
            PropertyDesc propertyDesc = getSinglePropertyDesc(classDesc,
                baseName, PropertyDesc.READ);
            ClassDesc typeClassDesc = prepareTypeClassDesc(propertyDesc);
            if (typeClassDesc != null) {
                return getPropertyDesc(typeClassDesc, name.substring(dot + 1),
                    mode);
            } else {
                return propertyDesc;
            }
        }
    }

    PropertyDesc getSinglePropertyDesc(ClassDesc classDesc, String name,
        int mode) {

        boolean array = false;
        int lparen = name.indexOf(STR_ARRAY_LPAREN);
        int rparen = name.indexOf(CHAR_ARRAY_RPAREN);
        if (lparen >= 0 && rparen > lparen) {
            array = true;
            name = name.substring(0, lparen);
        }
        PropertyDesc propertyDesc = classDesc.addProperty(name, mode);
        if (array) {
            // なるべく元の状態を壊さないようにこうしている。
            // （添字があるならば配列である、は真であるが、裏は真ではない。）
            propertyDesc.getTypeDesc().setArray(array);
        }
        return propertyDesc;
    }

    public ClassDesc prepareTypeClassDesc(PropertyDesc propertyDesc) {

        ClassDesc cd = propertyDesc.getTypeDesc().getClassDesc();
        ClassDesc returned = null;
        if (cd == TypeDesc.DEFAULT_CLASSDESC) {
            String name = propertyDesc.getName();
            if (propertyDesc.getTypeDesc().isArray()
                && name.endsWith(MULTIPLE_SUFFIX)) {
                // 名前を単数形にする。
                name = name.substring(0, name.length()
                    - MULTIPLE_SUFFIX.length());
            }
            returned = getTemporaryClassDesc(name);
            propertyDesc.getTypeDesc().setClassDesc(returned);
        } else if (cd instanceof ClassDescImpl) {
            returned = cd;
        }
        return returned;
    }
}
