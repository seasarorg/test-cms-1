package org.seasar.cms.ymir.extension.impl;

import java.lang.reflect.AnnotatedElement;

import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.extension.constraint.ConstraintFactory;
import org.seasar.cms.ymir.extension.constraint.ConstraintFactoryBase;

public class FufuConstraintFactory extends ConstraintFactoryBase implements
        ConstraintFactory<Fufu> {

    public Constraint getConstraint(Fufu annotation, AnnotatedElement element) {

        return new FufuConstraint(annotation.value());
    }
}
