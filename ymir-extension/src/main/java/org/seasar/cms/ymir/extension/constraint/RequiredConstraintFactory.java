package org.seasar.cms.ymir.extension.constraint;

import java.lang.reflect.AnnotatedElement;

import org.seasar.cms.ymir.Constraint;

public class RequiredConstraintFactory extends ConstraintFactoryBase implements ConstraintFactory<Required> {

    public Constraint getConstraint(Required annotation,
            AnnotatedElement element) {

        String[] names = add(annotation.value(), getPropertyName(element));
        if (names.length == 0) {
            throw new IllegalArgumentException(
                    "Spease specify at least one property: " + element);
        }
        return new RequiredConstraint(names);
    }
}
