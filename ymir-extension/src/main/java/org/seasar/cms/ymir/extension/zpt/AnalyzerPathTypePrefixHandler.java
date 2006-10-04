package org.seasar.cms.ymir.extension.zpt;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.VariableResolver.Entry;
import net.skirnir.freyja.zpt.tales.PathResolver;
import net.skirnir.freyja.zpt.tales.PathTypePrefixHandler;
import net.skirnir.freyja.zpt.tales.TalesExpressionEvaluator;

public class AnalyzerPathTypePrefixHandler extends PathTypePrefixHandler {

    private PathTypePrefixHandler delegated_;

    public AnalyzerPathTypePrefixHandler(PathTypePrefixHandler delegated) {
        delegated_ = delegated;
        delegated.addPathResolver(new AnalyzerPathResolver());
    }

    public PathTypePrefixHandler addPathResolver(PathResolver resolver) {
        // AnalyzerPathResolverを常に有効にするため、このメソッドの呼び出しは許可しない。
        throw new UnsupportedOperationException();
    }

    public Object handle(TemplateContext context, VariableResolver varResolver,
            String expr) {
        return delegated_.handle(context, varResolver, expr);
    }

    public void setTalesExpressionEvaluator(TalesExpressionEvaluator evaluator) {
        delegated_.setTalesExpressionEvaluator(evaluator);
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
