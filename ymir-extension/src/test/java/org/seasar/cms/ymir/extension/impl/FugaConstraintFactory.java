package org.seasar.cms.ymir.extension.impl;

import java.lang.reflect.AnnotatedElement;

import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.extension.constraint.ConstraintFactory;
import org.seasar.cms.ymir.extension.constraint.ConstraintFactoryBase;

public class FugaConstraintFactory extends ConstraintFactoryBase implements
        ConstraintFactory<Fuga> {

    public Constraint getConstraint(Fuga annotation, AnnotatedElement element) {

        return new FugaConstraint(annotation.value());
    }
}
