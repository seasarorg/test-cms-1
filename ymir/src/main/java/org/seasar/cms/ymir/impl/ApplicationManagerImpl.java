package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.framework.util.ArrayUtil;

public class ApplicationManagerImpl implements ApplicationManager {

    private Application[] applications_ = new Application[0];

    public void addApplication(Application application) {

        ArrayUtil.add(applications_, application);
    }

    public Application[] getApplications() {

        return applications_;
    }
}
