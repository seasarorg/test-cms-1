package org.seasar.cms.ymir.creator.impl;

import java.util.ArrayList;
import java.util.List;

public class WelcomeFileList {

    private List<String> welcomeFileList_ = new ArrayList<String>();

    public String[] getWelcomeFiles() {
        return welcomeFileList_.toArray(new String[0]);
    }

    public void addWelcomeFile(String welcomeFile) {
        welcomeFileList_.add(welcomeFile);
    }
}
