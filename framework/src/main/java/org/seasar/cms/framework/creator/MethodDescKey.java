package org.seasar.cms.framework.creator;

public class MethodDescKey {

    private MethodDesc methodDesc_;

    public MethodDescKey(MethodDesc methodDesc) {

        methodDesc_ = methodDesc;
    }

    public boolean equals(Object obj) {

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        MethodDescKey o = (MethodDescKey) obj;
        if (!o.methodDesc_.getName().equals(methodDesc_.getName())) {
            return false;
        }
        TypeDesc[] optds = o.methodDesc_.getParameterTypeDescs();
        TypeDesc[] ptds = methodDesc_.getParameterTypeDescs();
        if (optds.length != ptds.length) {
            return false;
        }
        for (int i = 0; i < optds.length; i++) {
            if (!optds[i].getTypeString().equals(ptds[i].getTypeString())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {

        return methodDesc_.getName().hashCode();
    }

    public MethodDesc getMethorDesc() {

        return methodDesc_;
    }
}
