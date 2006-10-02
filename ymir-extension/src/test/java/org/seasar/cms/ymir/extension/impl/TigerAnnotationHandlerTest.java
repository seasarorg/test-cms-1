package org.seasar.cms.ymir.extension.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.seasar.cms.ymir.Constraint;

public class TigerAnnotationHandlerTest extends TestCase {

    private TigerAnnotationHandler target_ = new TigerAnnotationHandler();

    public void testToAttributeName() throws Exception {

        assertEquals("attributeName", new TigerAnnotationHandler()
                .toAttributeName("setAttributeName", ""));
        assertEquals("attribute", new TigerAnnotationHandler().toAttributeName(
                "attribute", ""));
        assertEquals("hoe", new TigerAnnotationHandler().toAttributeName(
                "setAttributeName", "hoe"));
        assertEquals("hoeFuga", new TigerAnnotationHandler().toAttributeName(
                "attribute", "hoeFuga"));
    }

    public void testGetConstraints() throws Exception {

        List<Constraint> actual = new ArrayList<Constraint>();
        target_.getConstraint(Hoe.class, actual);

        assertEquals(1, actual.size());
        assertEquals("saru", ((FugaConstraint) actual.get(0)).getName());
    }

    /*
     * アクションを指定せず共通制約も指定しない場合は空の配列を返すこと。
     */
    public void testGetConstraints1() throws Exception {

        Hoe hoe = new Hoe();
        Constraint[] actual = target_.getConstraints(hoe, null, false);

        assertEquals(0, actual.length);
    }

    /*
     * アクションを指定せず共通制約を指定した場合は共通制約を返すこと。
     * ただしgetterの制約は含まないこと。
     */
    public void testGetConstraints2() throws Exception {

        Hoe hoe = new Hoe();
        Constraint[] actual = target_.getConstraints(hoe, null, true);

        assertEquals(2, actual.length);
        assertEquals("saru", ((FugaConstraint) actual[0]).getName());
        assertEquals("fuga", ((FugaConstraint) actual[1]).getName());
    }

    /*
     * アクションを指定して共通制約を指定しない場合はアクションの制約だけを返すこと。
     * ただしgetterの制約は含まないこと。
     */
    public void testGetConstraints3() throws Exception {

        Hoe hoe = new Hoe();
        Constraint[] actual = target_.getConstraints(hoe, Hoe.class.getMethod(
                "_render", new Class[0]), false);

        assertEquals(1, actual.length);
        assertEquals("render", ((FugaConstraint) actual[0]).getName());
    }

    /*
     * アクションを指定して共通制約を指定した場合はアクションの制約と共通制約を返すこと。
     * ただしgetterの制約は含まないこと。
     */
    public void testGetConstraints4() throws Exception {

        Hoe hoe = new Hoe();
        Constraint[] actual = target_.getConstraints(hoe, Hoe.class.getMethod(
                "_render", new Class[0]), true);

        assertEquals(3, actual.length);
        assertEquals("saru", ((FugaConstraint) actual[0]).getName());
        assertEquals("fuga", ((FugaConstraint) actual[1]).getName());
        assertEquals("render", ((FugaConstraint) actual[2]).getName());
    }

    /*
     * SuppressConstraintアノテーションが付与されている場合は共通制約を含まないこと。
     */
    public void testGetConstraints5() throws Exception {

        Hoe hoe = new Hoe();
        Constraint[] actual = target_.getConstraints(hoe, Hoe.class.getMethod(
                "_get", new Class[0]), true);

        assertEquals(1, actual.length);
        assertEquals("get", ((FugaConstraint) actual[0]).getName());
    }
}
