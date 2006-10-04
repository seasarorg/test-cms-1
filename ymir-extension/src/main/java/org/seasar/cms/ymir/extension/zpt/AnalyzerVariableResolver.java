package org.seasar.cms.ymir.extension.zpt;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;

public class AnalyzerVariableResolver implements VariableResolver {

    private VariableResolver delegated_;

    public AnalyzerVariableResolver(VariableResolver delegated) {
        delegated_ = delegated;
    }

    public boolean containsVariable(String name) {
        return delegated_.containsVariable(name);
    }

    public Object getVariable(TemplateContext context, String name) {
        AnalyzerContext analyzerContext = (AnalyzerContext) context;

        if (shouldGenerateClassOf(analyzerContext, name)) {
            return new DescWrapper(analyzerContext.getTemporaryClassDesc(name));
        } else {
            return delegated_.getVariable(context, name);
        }
    }

    boolean shouldGenerateClassOf(AnalyzerContext analyzerContext, String name) {
        Class type = Object.class;
        Entry entry = delegated_.getVariableEntry(analyzerContext, name);
        if (entry != null) {
            type = entry.getType();
        }
        return (type == Object.class || type.getName().startsWith(
                analyzerContext.getSourceCreator().getRootPackageName() + "."));
    }

    public Entry getVariableEntry(TemplateContext context, String name) {
        return delegated_.getVariableEntry(context, name);
    }

    public String[] getVariableNames() {
        return delegated_.getVariableNames();
    }

    public void removeVariable(String name) {
        delegated_.removeVariable(name);
    }

    public void setVariable(String name, Object value) {
        delegated_.setVariable(name, value);
    }
}
