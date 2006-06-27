package org.seasar.cms.framework.creator;

public class MethodDesc implements Cloneable {

    private String name_;

    private TypeDesc[] parameterTypeDescs_ = new TypeDesc[0];

    private TypeDesc returnTypeDesc_ = new TypeDesc(TypeDesc.TYPE_VOID);

    private String body_;

    public MethodDesc(String name) {

        name_ = name;
    }

    public Object clone() {

        MethodDesc cloned;
        try {
            cloned = (MethodDesc) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        if (parameterTypeDescs_ != null) {
            cloned.parameterTypeDescs_ = new TypeDesc[parameterTypeDescs_.length];
            for (int i = 0; i < parameterTypeDescs_.length; i++) {
                cloned.parameterTypeDescs_[i] = (TypeDesc) parameterTypeDescs_[i]
                    .clone();
            }
        }
        if (returnTypeDesc_ != null) {
            cloned.returnTypeDesc_ = (TypeDesc) returnTypeDesc_.clone();
        }

        return cloned;
    }

    public String getName() {

        return name_;
    }

    public TypeDesc getReturnTypeDesc() {

        return returnTypeDesc_;
    }

    public TypeDesc[] getParameterTypeDescs() {

        return parameterTypeDescs_;
    }

    public void setParameterTypeDescs(TypeDesc[] parameterTypeDescs) {

        parameterTypeDescs_ = parameterTypeDescs;
    }

    public String getBody() {

        return body_;
    }

    public void setBody(String body) {

        body_ = body;
    }
}
