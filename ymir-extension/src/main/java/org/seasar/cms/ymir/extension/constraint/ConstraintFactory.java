package org.seasar.cms.ymir.extension.constraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.seasar.cms.ymir.Constraint;

public interface ConstraintFactory<T extends Annotation> {

    Constraint getConstraint(T annotation, AnnotatedElement element);
}
