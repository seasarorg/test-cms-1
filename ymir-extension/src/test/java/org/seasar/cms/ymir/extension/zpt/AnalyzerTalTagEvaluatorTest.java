package org.seasar.cms.ymir.extension.zpt;

import junit.framework.TestCase;

import org.seasar.cms.ymir.extension.zpt.AnalyzerTalTagEvaluator.AnnotationResult;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.EvaluationRuntimeException;

public class AnalyzerTalTagEvaluatorTest extends TestCase {

    public void testFindAnnotation1() throws Exception {

        Attribute attr0 = new Attribute("name", "value", "\"");
        Attribute attr1 = new Attribute("tal:annotation", "annotation", "\"");
        Attribute attr2 = new Attribute("tal:attributes", "value", "\"");
        AnalyzerTalTagEvaluator evaluator = new AnalyzerTalTagEvaluator();
        AnnotationResult result = evaluator.findAnnotation(evaluator
                .newContext(), "tag", new Attribute[] { attr0, attr1, attr2, });
        assertEquals("annotation", result.getAnnotation());
        Attribute[] attrs = result.getTheOtherAttributes();
        assertEquals(2, attrs.length);
        assertSame(attr0, attrs[0]);
        assertSame(attr2, attrs[1]);
    }

    public void testFindAnnotation2() throws Exception {

        Attribute attr0 = new Attribute("name", "value", "\"");
        Attribute attr1 = new Attribute("tal:attributes", "value", "\"");
        AnalyzerTalTagEvaluator evaluator = new AnalyzerTalTagEvaluator();
        AnnotationResult result = evaluator.findAnnotation(evaluator
                .newContext(), "tag", new Attribute[] { attr0, attr1, });
        assertNull(result.getAnnotation());
        Attribute[] attrs = result.getTheOtherAttributes();
        assertEquals(2, attrs.length);
        assertSame(attr0, attrs[0]);
        assertSame(attr1, attrs[1]);
    }

    public void testFindAnnotation3() throws Exception {

        Attribute attr0 = new Attribute("name", "value", "\"");
        Attribute attr1 = new Attribute("tal:annotation", "annotation", "\"");
        Attribute attr2 = new Attribute("tal:attributes", "value", "\"");
        Attribute attr3 = new Attribute("tal:annotation", "annotation", "\"");
        AnalyzerTalTagEvaluator evaluator = new AnalyzerTalTagEvaluator();
        try {
            evaluator.findAnnotation(evaluator.newContext(), "tag",
                    new Attribute[] { attr0, attr1, attr2, attr3 });
            fail();
        } catch (EvaluationRuntimeException expected) {
        }
    }

    public void testToAbsolutePath() throws Exception {

        AnalyzerTalTagEvaluator evaluator = new AnalyzerTalTagEvaluator();

        assertEquals("/path/to/page.html", evaluator.toAbsolutePath(
                "/path/to/page.html", ""));

        assertEquals("/path/to/page2.html", evaluator.toAbsolutePath(
                "/path/to/page.html", "page2.html"));

        assertEquals("/path/page2.html", evaluator.toAbsolutePath(
                "/path/to/page.html", "../page2.html"));
    }
}
