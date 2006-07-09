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
        ParameterDesc[] opds = o.methodDesc_.getParameterDescs();
        ParameterDesc[] pds = methodDesc_.getParameterDescs();
        if (opds.length != pds.length) {
            return false;
        }
        for (int i = 0; i < opds.length; i++) {
            if (!opds[i].getTypeDesc().getName().equals(
                pds[i].getTypeDesc().getName())) {
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
