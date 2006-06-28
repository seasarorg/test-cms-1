package org.seasar.cms.framework.creator;

public class MethodDesc implements Cloneable {

    private String name_;

    private TypeDesc[] parameterTypeDescs_ = new TypeDesc[0];

    private TypeDesc returnTypeDesc_ = new TypeDesc(TypeDesc.TYPE_VOID);

    private BodyDesc bodyDesc_;

    private String evaluatedBody_;

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
        if (bodyDesc_ != null) {
            cloned.bodyDesc_ = (BodyDesc) bodyDesc_.clone();
        }

        return cloned;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(returnTypeDesc_).append(" ").append(name_).append("(");
        String delim = "";
        for (int i = 0; i < parameterTypeDescs_.length; i++) {
            sb.append(delim).append(parameterTypeDescs_[i]);
            delim = ", ";
        }
        sb.append(")");
        return sb.toString();
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

    public BodyDesc getBodyDesc() {

        return bodyDesc_;
    }

    public void setBodyDesc(BodyDesc bodyDesc) {

        bodyDesc_ = bodyDesc;
    }

    public String getEvaluatedBody() {

        return evaluatedBody_;
    }

    public void setEvaluatedBody(String evaluatedBody) {

        evaluatedBody_ = evaluatedBody;
    }
}
