package org.seasar.cms.ymir;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import org.seasar.cms.pluggable.hotdeploy.LocalHotdeployS2Container;
import org.seasar.framework.container.S2Container;

public interface Application {

    String getId();

    String getWebappRoot();

    String getWebappSourceRoot();

    Class getReferenceClass();

    String getProjectRoot();

    void setProjectRoot(String projectRoot);

    String getRootPackageName();

    void setRootPackageName(String rootPackageName);

    String getSourceDirectory();

    void setSourceDirectory(String sourceDirectory);

    String getResourcesDirectory();

    void setResourcesDirectory(String resourcesDirectory);

    boolean isResourceExists(String path);

    S2Container getS2Container();

    boolean isCapable(Class clazz);

    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    Enumeration propertyNames();

    void setProperty(String key, String value);

    void removeProperty(String key);

    void save(OutputStream out, String header) throws IOException;

    String getDefaultPropertiesFilePath();

    LocalHotdeployS2Container getHotdeployS2Container();

    PathMappingProvider getPathMappingProvider();

    /**
     * このアプリケーションが開発中のステータスであるかどうかを返します。
     * <p>このメソッドは、このアプリケーションが開発中のステータスかどうかを返すだけであって、
     * Ymir自体が開発中のステータスを持つかどうかとは無関係であることに注意して下さい。
     * 仮にこのメソッドがtrueを返しても、Ymir自体が開発中でない場合はアプリケーションを開発中と
     * みなすべきではありません。
     * Ymirのステータスを含めてこのアプリケーションを開発中とみなしてよいかどうかを知るためには、
     * {@link org.seasar.cms.ymir.Ymir#isUnderDevelopment()}を使用して下さい。
     * </p>
     *
     * @return このアプリケーションが開発中のステータスであるかどうか。
     */
    boolean isUnderDevelopment();
}
