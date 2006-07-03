package org.seasar.cms.framework.creator;

import java.io.File;

import junit.framework.TestCase;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.impl.TypeDescImpl;

public class DescValidatorTest extends TestCase {

    private SourceCreator sourceCreator_;

    protected void setUp() throws Exception {

        sourceCreator_ = new SourceCreator() {

            public String getPagePackageName() {
                return null;
            }

            public String getDtoPackageName() {
                return null;
            }

            public String getComponentName(String path, String method) {
                return null;
            }

            public String getActionName(String path, String method) {
                return null;
            }

            public String getClassName(String componentName) {
                return null;
            }

            public File getSourceFile(String className) {
                return new File("ZETTAIARIENAIFILEPATH");
            }

            public File getTemplateFile(String className) {
                return null;
            }

            public Response update(String path, String method, Request request) {
                return null;
            }

            public String getDaoPackageName() {
                return null;
            }

            public String getDxoPackageName() {
                return null;
            }
        };
    }

    public void testIsValid1() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl(TypeDesc.TYPE_VOID);

        // ## Act ##
        boolean actual = DescValidator.isValid(target, sourceCreator_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid2() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("int");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, sourceCreator_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid3() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("String");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, sourceCreator_);

        // ## Assert ##
        assertFalse(actual);
    }

    public void testIsValid4() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.String");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, sourceCreator_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid5() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.String[]");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, sourceCreator_);

        // ## Assert ##
        assertTrue(actual);
    }

    public void testIsValid6() throws Exception {

        // ## Arrange ##
        TypeDesc target = new TypeDescImpl("java.lang.Hoehoe");

        // ## Act ##
        boolean actual = DescValidator.isValid(target, sourceCreator_);

        // ## Assert ##
        assertFalse(actual);
    }
}
