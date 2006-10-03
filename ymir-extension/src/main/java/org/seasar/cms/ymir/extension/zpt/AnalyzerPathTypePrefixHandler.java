package org.seasar.cms.ymir.extension.zpt;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.VariableResolver.Entry;
import net.skirnir.freyja.zpt.tales.PathTypePrefixHandler;

public class AnalyzerPathTypePrefixHandler extends PathTypePrefixHandler {

    public AnalyzerPathTypePrefixHandler(char pathExpDelim) {

        super(pathExpDelim);
    }

    protected Object getProperty(TemplateContext context,
            VariableResolver varResolver, String arg) {

        AnalyzerContext analyzerContext = toAnalyzerContext(context);

        Object value = super.getProperty(context, varResolver, arg);
        if (shouldGenerateClassOf(value, analyzerContext, varResolver, arg)) {
            return new DescWrapper(analyzerContext.getTemporaryClassDesc(arg));
        } else {
            return value;
        }
    }

    boolean shouldGenerateClassOf(Object value,
            AnalyzerContext analyzerContext, VariableResolver varResolver,
            String arg) {

        Class type = Object.class;
        Entry entry = varResolver.getVariableEntry(analyzerContext, arg);
        if (entry != null) {
            type = entry.getType();
        }
        return (type == Object.class || type.getName().startsWith(
                analyzerContext.getSourceCreator().getRootPackageName() + "."));
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
