package org.seasar.cms.ymir.beantable;

import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.cms.ymir.container.ClassTraverser;

public class BeantableLifecycleListener implements LifecycleListener {

    private ClassTraverser classTraverser_;

    public void init() {

        classTraverser_.traverse();
    }

    public void destroy() {
    }

    public void setClassTraverser(ClassTraverser classTraverser) {

        classTraverser_ = classTraverser;
    }
}
