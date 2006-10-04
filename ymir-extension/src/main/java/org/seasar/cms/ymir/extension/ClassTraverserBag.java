package org.seasar.cms.ymir.extension;

import org.seasar.cms.pluggable.ClassTraverser;
import org.seasar.cms.ymir.Application;

public class ClassTraverserBag {

    private ClassTraverser classTraverser_;

    private Application application_;

    public ClassTraverserBag(ClassTraverser classTraverser,
            Application application) {
        classTraverser_ = classTraverser;
        application_ = application;
    }

    public Application getApplication() {
        return application_;
    }

    public ClassTraverser getClassTraverser() {
        return classTraverser_;
    }
}
