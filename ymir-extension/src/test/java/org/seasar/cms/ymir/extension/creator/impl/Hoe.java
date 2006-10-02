package org.seasar.cms.ymir.extension.creator.impl;

public class Hoe {

    @Annotation3
    public void method1() {
    }

    @Annotation4()
    public void method2() {
    }

    @Annotation4("")
    public void method3() {
    }

    @Annotation4( { "", "" })
    public void method4() {
    }

    @Annotation1(intValue = 1)
    public void method5() {
    }

    @Annotation1(byteValue = 1)
    public void method6() {
    }

    @Annotation1(charValue = 1)
    public void method7() {
    }

    @Annotation1(shortValue = 1)
    public void method8() {
    }

    @Annotation1(longValue = 1)
    public void method9() {
    }

    @Annotation1(floatValue = 1)
    public void method10() {
    }

    @Annotation1(doubleValue = 1)
    public void method11() {
    }

    @Annotation1(booleanValue = true)
    public void method12() {
    }

    @Annotation1(enumValue = Enum1.VALUE2)
    public void method13() {
    }

    @Annotation1(classValue = String.class)
    public void method14() {
    }

    @Annotation2(annotation = @Annotation3)
    public void method15() {
    }
}
