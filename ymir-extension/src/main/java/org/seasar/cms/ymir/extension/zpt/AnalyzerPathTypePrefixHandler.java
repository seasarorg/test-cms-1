package org.seasar.cms.ymir.extension.zpt;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.PathTypePrefixHandler;

public class AnalyzerPathTypePrefixHandler extends PathTypePrefixHandler {

    public AnalyzerPathTypePrefixHandler(char pathExpDelim) {

        super(pathExpDelim);
    }

    protected Object getProperty(TemplateContext context,
        VariableResolver varResolver, String arg) {

        return new DescWrapper(toAnalyzerContext(context)
            .getTemporaryClassDesc(arg));
    }

    protected Object resolveSegment(TemplateContext context,
        VariableResolver varResolver, Object obj, String segment) {

        if (obj instanceof DescWrapper) {
            DescWrapper wrapper = (DescWrapper) obj;
            ClassDesc classDesc = wrapper.getClassDesc();
            int mode = (classDesc.isKindOf(ClassDesc.KIND_DTO) ? (PropertyDesc.READ | PropertyDesc.WRITE)
                : PropertyDesc.READ);
            return new DescWrapper(toAnalyzerContext(context), classDesc
                .addProperty(segment, mode));
        } else {
            return super.resolveSegment(context, varResolver, obj, segment);
        }
    }

    AnalyzerContext toAnalyzerContext(TemplateContext context) {

        return (AnalyzerContext) context;
    }
}
