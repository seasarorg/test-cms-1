package org.seasar.cms.framework;

public interface Configuration {

    String PROJECTSTATUS_DEVELOP = "develop";

    String PROJECTSTATUS_RELEASE = "release";

    String KEY_PROJECTROOT = "projectRoot";

    String KEY_PROJECTSTATUS = "projectStatus";

    String KEY_ROOTPACKAGENAME = "rootPackageName";

    String getProperty(String key);

    String getProperty(String key, String defaultValue);
}
