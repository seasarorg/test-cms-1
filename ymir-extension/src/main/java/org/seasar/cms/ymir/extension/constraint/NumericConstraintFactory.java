package org.seasar.cms.ymir.extension.constraint;

import java.lang.reflect.AnnotatedElement;

import org.seasar.cms.ymir.Constraint;

public class NumericConstraintFactory extends ConstraintFactoryBase implements
        ConstraintFactory<Numeric> {

    public Constraint getConstraint(Numeric annotation, AnnotatedElement element) {

        String[] names = add(annotation.value(), annotation.property(),
                getPropertyName(element));
        if (names.length == 0) {
            throw new IllegalArgumentException(
                    "Spease specify at least one property: " + element);
        }

        boolean greaterIncludeEqual = false;
        Double greaterEdge = annotation.greaterThan();
        if (greaterEdge == Double.MIN_VALUE) {
            greaterEdge = annotation.greaterEqual();
            greaterIncludeEqual = true;
            if (greaterEdge == Double.MIN_VALUE) {
                greaterEdge = null;
            }
        }

        boolean lessIncludeEqual = false;
        Double lessEdge = annotation.lessThan();
        if (lessEdge == Double.MAX_VALUE) {
            lessEdge = annotation.lessEqual();
            lessIncludeEqual = true;
            if (lessEdge == Double.MAX_VALUE) {
                lessEdge = null;
            }
        }

        return new NumericConstraint(names, annotation.integer(), greaterEdge,
                greaterIncludeEqual, lessEdge, lessIncludeEqual);
    }
}
