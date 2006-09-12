package org.seasar.cms.ymir;

import org.seasar.kvasir.util.el.VariableResolver;

public class MatchedPathMapping {

    private PathMapping pathMapping_;

    private VariableResolver variableResolver_;

    public MatchedPathMapping() {
    }

    public MatchedPathMapping(PathMapping pathMapping,
            VariableResolver variableResolver) {

        setPathMapping(pathMapping);
        setVariableResolver(variableResolver);
    }

    public PathMapping getPathMapping() {
        return pathMapping_;
    }

    public void setPathMapping(PathMapping pathMapping) {
        pathMapping_ = pathMapping;
    }

    public VariableResolver getVariableResolver() {
        return variableResolver_;
    }

    public void setVariableResolver(VariableResolver variableResolver) {
        variableResolver_ = variableResolver;
    }

    public String getComponentName() {
        return pathMapping_.getComponentName(variableResolver_);
    }

    public String getActionName() {
        return pathMapping_.getActionName(variableResolver_);
    }

    public String getPathInfo() {
        return pathMapping_.getPathInfo(variableResolver_);

    }

    public String getDefaultPath() {
        return pathMapping_.getDefaultPath(variableResolver_);
    }

    public boolean isDenied() {
        return pathMapping_.isDenied();
    }
}
