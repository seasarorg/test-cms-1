package org.seasar.cms.ymir.extension.constraint;

import java.lang.reflect.AnnotatedElement;

import org.seasar.cms.ymir.Constraint;

public class TokenRequiredConstraintFactory extends ConstraintFactoryBase
        implements ConstraintFactory<TokenRequired> {

    public Constraint getConstraint(TokenRequired annotation,
            AnnotatedElement element) {

        return new TokenRequiredConstraint(annotation.value());
    }
}
