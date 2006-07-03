package org.seasar.cms.framework.zpt;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.PathTypePrefixHandler;

public class AnalyzerPathTypePrefixHandler extends PathTypePrefixHandler {

    public AnalyzerPathTypePrefixHandler(char pathExpDelim) {

        super(pathExpDelim);
    }

    protected Object getProperty(TemplateContext context,
        VariableResolver varResolver, String arg) {

        AnalyzerContext analyzerContext = toAnalyzerContext(context);

        ClassDesc classDesc = analyzerContext.getTemporaryClassDesc(arg);
        //        if (!analyzerContext.isEmptyDto(classDesc)) {
        return classDesc;
        //        } else {
        //            Object value = super.getProperty(context, varResolver, arg);
        //            if (value == null) {
        //                // 極力attributeを解釈させるためのダミー。
        //                value = "0";
        //            }
        //            return value;
        //        }
    }

    protected Object resolveSegment(TemplateContext context,
        VariableResolver varResolver, Object obj, String segment) {

        AnalyzerContext analyzerContext = toAnalyzerContext(context);
        ClassDesc classDesc = null;
        if (obj instanceof ClassDesc) {
            classDesc = (ClassDesc) obj;
        } else if (obj instanceof PropertyDesc) {
            classDesc = analyzerContext
                .prepareTypeClassDesc((PropertyDesc) obj);
        }
        if (classDesc != null) {
            int mode = (ClassDesc.KIND_DTO.equals(classDesc.getKind()) ? (PropertyDesc.READ | PropertyDesc.WRITE)
                : PropertyDesc.READ);
            return classDesc.addProperty(segment, mode);
        } else {
            return super.resolveSegment(context, varResolver, obj, segment);
        }
    }

    AnalyzerContext toAnalyzerContext(TemplateContext context) {

        return (AnalyzerContext) context;
    }
}
