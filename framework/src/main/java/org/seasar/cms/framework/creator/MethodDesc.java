package org.seasar.cms.framework.creator;

public interface MethodDesc extends Cloneable {

    Object clone();

    String getName();

    TypeDesc getReturnTypeDesc();

    ParameterDesc[] getParameterDescs();

    void setParameterDescs(ParameterDesc[] parameterDescs);

    BodyDesc getBodyDesc();

    void setBodyDesc(BodyDesc bodyDesc);

    String getEvaluatedBody();

    void setEvaluatedBody(String evaluatedBody);
}
