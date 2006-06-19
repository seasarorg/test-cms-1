package org.seasar.cms.framework.creator.impl;

class Condition {

    private boolean classBound_;

    private boolean classExists_;

    private boolean templateExists_;

    private String method_;

    public Condition(boolean classBound, boolean classExists,
        boolean templateExists, String method) {

        classBound_ = classBound;
        classExists_ = classExists;
        templateExists_ = templateExists;
        method_ = method;
    }

    public int hashCode() {

        int code = 0;
        code += (classBound_ ? 1 : 0);
        code += (classExists_ ? 1 : 0);
        code += (templateExists_ ? 1 : 0);
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
}
