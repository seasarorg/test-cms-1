package org.seasar.cms.ymir.creator;

import java.util.HashSet;
import java.util.Set;

public class ClassDescSet {

    private Set<String> classNameSet_ = new HashSet<String>();

    public ClassDescSet() {
    }

    public ClassDescSet(ClassDesc[] classDescs) {

        for (int i = 0; i < classDescs.length; i++) {
            add(classDescs[i]);
        }
    }

    public void add(ClassDesc classDesc) {

        classNameSet_.add(classDesc.getName());
    }

    public boolean contains(ClassDesc classDesc) {

        return contains(classDesc.getName());
    }

    public boolean contains(String className) {

        return classNameSet_.contains(className);
    }

    public void remove(String className) {

        classNameSet_.remove(className);
    }
}
