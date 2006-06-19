package org.seasar.cms.framework.creator;

public class MethodDesc extends AbstractDesc {

    private String name_;

    private String returnType_;

    private String defaultReturnType_;

    private String body_;

    public MethodDesc(String name) {

        name_ = name;
    }

    public String getName() {

        return name_;
    }

    public String getReturnType() {

        return returnType_;
    }

    public void setReturnType(String returnType) {

        returnType_ = returnType;
    }

    public String getDefaultReturnType() {

        return defaultReturnType_;
    }

    public void setDefaultReturnType(String defaultReturnType) {

        defaultReturnType_ = defaultReturnType;
    }

    public String getReturnTypeString() {

        return getTypeString(returnType_, defaultReturnType_, TYPE_VOID);
    }

    public String getBody() {

        return body_;
    }

    public void setBody(String body) {

        body_ = body;
    }
}
