package org.seasar.cms.framework.creator.impl;

import java.util.ArrayList;
import java.util.List;

public class WelcomeFileList {

    private List welcomeFileList_ = new ArrayList();

    public String[] getWelcomeFiles() {
        return (String[]) welcomeFileList_.toArray(new String[0]);
    }

    public void addWelcomeFile(String welcomeFile) {
        welcomeFileList_.add(welcomeFile);
    }
}
