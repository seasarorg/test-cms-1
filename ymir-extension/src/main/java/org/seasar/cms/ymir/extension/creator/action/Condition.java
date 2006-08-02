package org.seasar.cms.ymir.extension.creator.action;

public class Condition {

    private State classBound_;

    private State classExists_;

    private State templateExists_;

    private String method_;

    public Condition(State classBound, State classExists, State templateExists,
        String method) {

        classBound_ = classBound;
        classExists_ = classExists;
        templateExists_ = templateExists;
        method_ = method;
    }

    public int hashCode() {

        int code = 0;
        code += classBound_.ordinal();
        code += classExists_.ordinal();
        code += templateExists_.ordinal();
        code += (method_ != null ? method_.hashCode() : 0);
        return code;
    }

    public boolean equals(Object obj) {

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Condition o = (Condition) obj;
        if (o.classBound_ != classBound_) {
            return false;
        }
        if (o.classExists_ != classExists_) {
            return false;
        }
        if (o.templateExists_ != templateExists_) {
            return false;
        }
        if (o.method_ == null) {
            if (method_ != null) {
                return false;
            }
        } else if (!o.method_.equalsIgnoreCase(method_)) {
            return false;
        }

        return true;
    }

    public State getClassBound() {
        return classBound_;
    }

    public State getClassExists() {
        return classExists_;
    }

    public State getTemplateExists() {
        return templateExists_;
    }

    public String getMethod() {
        return method_;
    }
}
