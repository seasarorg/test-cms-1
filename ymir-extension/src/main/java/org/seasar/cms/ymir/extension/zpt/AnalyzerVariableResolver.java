package org.seasar.cms.ymir.zpt;

import java.util.HashMap;

import org.seasar.cms.ymir.impl.DefaultRequestProcessor;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;

public class AnalyzerVariableResolver implements VariableResolver {

    private VariableResolver resolver_;

    public AnalyzerVariableResolver(VariableResolver resolver) {

        resolver_ = resolver;
    }

    public Object getVariable(TemplateContext context, String name) {
        if (DefaultRequestProcessor.ATTR_PAGE.equals(name)) {
            return new HashMap();
        }
        return resolver_.getVariable(context, name);
    }

    public String[] getVariableNames() {
        return resolver_.getVariableNames();
    }

    public Class getVariableType(String name) {
        return resolver_.getVariableType(name);
    }

    public void setVariable(String name, Object value) {
        resolver_.setVariable(name, value);
    }
}
