package org.seasar.cms.ymir.extension.zpt;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.FormDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.TypeDesc;
import org.seasar.cms.ymir.extension.creator.impl.ClassDescImpl;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;

import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.ZptTemplateContext;

public class AnalyzerContext extends ZptTemplateContext {

    private static final String MULTIPLE_SUFFIX = "s";

    private static final char STR_ARRAY_LPAREN = '[';

    private static final char CHAR_ARRAY_RPAREN = ']';

    private SourceCreator sourceCreator_;

    private String method_;

    private Map<String, ClassDesc> classDescMap_;

    private Map<String, ClassDesc> temporaryClassDescMap_ = new LinkedHashMap<String, ClassDesc>();

    private String pageClassName_;

    private FormDesc formDesc_;

    private boolean usingFreyjaRenderClasses_;

    private String rootPackageName_;

    private VariableResolver variableResolver_;

    private Set<String> usedWithVariableSet_ = new HashSet<String>();

    private Set<String> usedWithLocalVariableSet_ = new HashSet<String>();

    @Override
    public VariableResolver getVariableResolver() {
        if (variableResolver_ == null) {
            variableResolver_ = new AnalyzerVariableResolver(super
                    .getVariableResolver());
        }
        return variableResolver_;
    }

    @Override
    public void setVariableResolver(VariableResolver varResolver) {
        super.setVariableResolver(varResolver);
        variableResolver_ = null;
    }

    public void setUsingFreyjaRenderClasses(boolean usingFreyjaRenderClasses) {
        usingFreyjaRenderClasses_ = usingFreyjaRenderClasses;
    }

    public void defineVariable(int scope, String name, Object value) {

        if (value != null && value instanceof DescWrapper) {
            DescWrapper wrapper = (DescWrapper) value;
            PropertyDesc propertyDesc = wrapper.getPropertyDesc();
            if (propertyDesc != null) {
                TypeDesc typeDesc = propertyDesc.getTypeDesc();
                ClassDesc classDescOfdefinedVariable = getTemporaryClassDesc(name);
                setUsedWithLocalVariable(classDescOfdefinedVariable);
                typeDesc.setClassDesc(classDescOfdefinedVariable);
            } else {
                // self/entitiesのような形式ではなく、直接entitiesのように式が書かれている。
                // 自動生成ではそのようなプロパティは今のところ扱わない。
                ;
            }
        }

        super.defineVariable(scope, name, value);
    }

    public RepeatInfo pushRepeatInfo(String name, Object[] objs) {

        if (objs != null && objs.length == 1 && objs[0] instanceof DescWrapper) {
            DescWrapper wrapper = (DescWrapper) objs[0];
            PropertyDesc propertyDesc = wrapper.getPropertyDesc();
            if (propertyDesc != null) {
                TypeDesc typeDesc = propertyDesc.getTypeDesc();
                typeDesc.setArray(true);
                ClassDesc classDescOfDefinedVariable = getTemporaryClassDesc(name);
                setUsedWithLocalVariable(classDescOfDefinedVariable);
                typeDesc.setClassDesc(classDescOfDefinedVariable);
            } else {
                // self/entitiesのような形式ではなく、直接entitiesのように式が書かれている。
                // 自動生成ではそのようなプロパティは今のところ扱わない。
                ;
            }
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

    public ClassDesc getTemporaryClassDesc(String name) {

        String className;
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            if (usingFreyjaRenderClasses_) {
                String renderClassName = "net.skirnir.freyja.render."
                        + capFirst(name);
                if (isAvailable(renderClassName)) {
                    className = renderClassName;
                } else {
                    renderClassName = "net.skirnir.freyja.render.html."
                            + capFirst(name) + "Tag";
                    if (isAvailable(renderClassName)) {
                        className = renderClassName;
                    } else {
                        className = toClassName(name);
                    }
                }
            } else {
                className = toClassName(name);
            }
        } else {
            className = name;
        }
        ClassDesc classDesc = (ClassDesc) temporaryClassDescMap_.get(className);
        if (classDesc == null) {
            classDesc = sourceCreator_.newClassDesc(className);
            temporaryClassDescMap_.put(className, classDesc);
        }
        return classDesc;
    }

    boolean isAvailable(String className) {

        return (sourceCreator_.getClass(className) != null);
    }

    public String toClassName(String componentName) {

        String className = null;
        if (DefaultRequestProcessor.ATTR_SELF.equals(componentName)) {
            className = getPageClassName();
            // Kvasir/SoraのpopプラグインのexternalTemplate機能を使って自動生成
            // をしている場合、classNameはnullになり得ることに注意。
        } else {
            className = sourceCreator_.getClassName(componentName);
        }
        if (className == null) {
            className = getDtoClassName(componentName);
        }
        return className;
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

    public FormDesc getFormDesc() {

        return formDesc_;
    }

    public void setFormDesc(FormDesc formDesc) {

        formDesc_ = formDesc;
    }

    public void close() {

        for (Iterator<ClassDesc> itr = temporaryClassDescMap_.values()
                .iterator(); itr.hasNext();) {
            ClassDesc classDesc = itr.next();
            if (isOuter(classDesc) || isEmptyDto(classDesc)) {
                // 自動生成対象外のクラスと中身のないDTOは除外しておく。
                itr.remove();
            }
        }

        for (Iterator<Map.Entry<String, ClassDesc>> itr = temporaryClassDescMap_
                .entrySet().iterator(); itr.hasNext();) {
            Map.Entry<String, ClassDesc> entry = itr.next();
            String name = entry.getKey();
            ClassDesc classDesc = entry.getValue();

            // プロパティの型に対応するDTOがMapに存在しない場合は、
            // そのDTOは上のフェーズで除外された、すなわちDTOかもしれないと考えて
            // 解析を進めたが結局DTOであることが確定しなかったので、
            // 型をデフォルトクラスに差し替える。
            PropertyDesc[] pds = classDesc.getPropertyDescs();
            for (int i = 0; i < pds.length; i++) {
                ClassDesc typeClassDesc = pds[i].getTypeDesc().getClassDesc();
                if (isDto(typeClassDesc)
                        && !temporaryClassDescMap_.containsKey(typeClassDesc
                                .getName())) {
                    pds[i].getTypeDesc().setClassDesc(
                            TypeDesc.DEFAULT_CLASSDESC);
                }
            }
            classDesc.merge(classDescMap_.get(name));
        }

        for (Iterator<ClassDesc> itr = temporaryClassDescMap_.values()
                .iterator(); itr.hasNext();) {
            ClassDesc classDesc = itr.next();

            // PageクラスとPageクラスから利用されているDTOと、外部で定義されている変数の型クラス
            // 以外は無視する。
            if (isPage(classDesc)) {
                registerAvailablePagesAndDtos(classDesc);
            } else if (isUsedWithVariable(classDesc)
                    && !isUsedWithLocalVariable(classDesc)) {
                register(classDesc);
            }
        }
    }

    void registerAvailablePagesAndDtos(ClassDesc classDesc) {

        register(classDesc);
        PropertyDesc[] pds = classDesc.getPropertyDescs();
        for (int i = 0; i < pds.length; i++) {
            ClassDesc typeClassDesc = pds[i].getTypeDesc().getClassDesc();
            if (isDto(typeClassDesc)) {
                registerAvailablePagesAndDtos(typeClassDesc);
            }
        }
    }

    void register(ClassDesc classDesc) {

        classDescMap_.put(classDesc.getName(), classDesc);
    }

    boolean isOuter(ClassDesc classDesc) {
        return !classDesc.getPackageName().startsWith(
                sourceCreator_.getRootPackageName() + ".");
    }

    boolean isPage(ClassDesc classDesc) {

        return ClassDesc.KIND_PAGE.equals(classDesc.getKind());
    }

    boolean isDto(ClassDesc classDesc) {

        return ClassDesc.KIND_DTO.equals(classDesc.getKind());
    }

    boolean isEmptyDto(ClassDesc classDesc) {

        return (isDto(classDesc) && classDesc.isEmpty());
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

    public boolean isUsedWithVariable(ClassDesc classDesc) {

        return usedWithVariableSet_.contains(classDesc.getName());
    }

    public void setUsedWithVariable(ClassDesc classDesc) {

        usedWithVariableSet_.add(classDesc.getName());
    }

    public boolean isUsedWithLocalVariable(ClassDesc classDesc) {

        return usedWithLocalVariableSet_.contains(classDesc.getName());
    }

    public void setUsedWithLocalVariable(ClassDesc classDesc) {

        usedWithLocalVariableSet_.add(classDesc.getName());
    }
}
