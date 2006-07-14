package org.seasar.cms.ymir.zpt;

import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.PropertyDesc;

public class DescWrapper {

    private AnalyzerContext analyzerContext_;

    private ClassDesc classDesc_;

    private PropertyDesc propertyDesc_;

    public DescWrapper(ClassDesc classDesc) {

        classDesc_ = classDesc;
    }

    public DescWrapper(AnalyzerContext analyzerContext,
        PropertyDesc propertyDesc) {

        analyzerContext_ = analyzerContext;
        propertyDesc_ = propertyDesc;
    }

    public String toString() {

        return "0";
    }

    public ClassDesc getClassDesc() {

        if (classDesc_ != null) {
            return classDesc_;
        } else {
            return analyzerContext_.prepareTypeClassDesc(propertyDesc_);
        }
    }

    public PropertyDesc getPropertyDesc() {

        return propertyDesc_;
    }
}
