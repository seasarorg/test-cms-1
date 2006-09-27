package org.seasar.cms.ymir.extension.creator;

public class MethodDescKey implements Comparable<MethodDescKey> {

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

    public int compareTo(MethodDescKey o) {

        int cmp = methodDesc_.getName().compareTo(o.methodDesc_.getName());
        if (cmp != 0) {
            return cmp;
        }
        ParameterDesc[] opds = o.methodDesc_.getParameterDescs();
        ParameterDesc[] pds = methodDesc_.getParameterDescs();
        int n = (opds.length > pds.length ? pds.length : opds.length);
        for (int i = 0; i < n; i++) {
            cmp = opds[i].getTypeDesc().getName().compareTo(
                    pds[i].getTypeDesc().getName());
            if (cmp != 0) {
                return cmp;
            }
        }
        return (pds.length - opds.length);
    }
}
