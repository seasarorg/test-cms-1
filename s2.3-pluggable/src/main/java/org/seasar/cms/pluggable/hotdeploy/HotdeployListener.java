package org.seasar.cms.pluggable.hotdeploy;

public interface HotdeployListener {

    void definedClass(Class<?> clazz);
}
