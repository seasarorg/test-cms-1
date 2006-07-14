package org.seasar.cms.ymir;

import java.io.IOException;
import java.io.OutputStream;

public interface Configuration {

    String PROJECTSTATUS_DEVELOP = "develop";

    String PROJECTSTATUS_RELEASE = "release";

    String KEY_PROJECTROOT = "projectRoot";

    String KEY_WEBAPPROOT = "webappRoot";

    String KEY_PROJECTSTATUS = "projectStatus";

    String KEY_ROOTPACKAGENAME = "rootPackageName";

    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    void setProperty(String key, String value);

    void removeProperty(String key);

    void save(OutputStream out, String header) throws IOException;
    
    boolean equalsProjectStatus(String status);
}
