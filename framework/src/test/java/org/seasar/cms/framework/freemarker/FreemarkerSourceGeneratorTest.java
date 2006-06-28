package org.seasar.cms.framework.freemarker;

import java.io.File;

import org.seasar.cms.framework.FrameworkTestCase;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;

public class FreemarkerSourceGeneratorTest extends FrameworkTestCase {

    private FreemarkerSourceGenerator target_;

    protected void setUp() throws Exception {

        super.setUp();
        target_ = new FreemarkerSourceGenerator();
        target_.setSourceCreator(new SourceCreator() {

            public String getPagePackageName() {
                return "com.example.web";
            }

            public String getDtoPackageName() {
                return "com.example.dto";
            }

            public String getDaoPackageName() {
                return "com.example.dao";
            }

            public String getDxoPackageName() {
                return "com.example.dxo";
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
                return null;
            }

            public File getTemplateFile(String className) {
                return null;
            }

            public Response update(String path, String method, Request request) {
                return null;
            }
        });
    }

    private ClassDesc prepareClassDesc() {

        ClassDesc classDesc = new ClassDesc("com.example.page.TestPage");
        classDesc.setSuperclassName("com.example.page.SuperPage");
        PropertyDesc propertyDesc = new PropertyDesc("param1");
        propertyDesc.getTypeDesc().setDefaultType("boolean");
        propertyDesc.setMode(PropertyDesc.READ);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDesc("param2");
        propertyDesc.setMode(PropertyDesc.WRITE);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDesc("param3");
        propertyDesc.setMode(PropertyDesc.READ | PropertyDesc.WRITE);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDesc("param4");
        propertyDesc.setMode(PropertyDesc.READ | PropertyDesc.WRITE);
        propertyDesc.getTypeDesc().setType("java.lang.Integer[]");
        classDesc.setPropertyDesc(propertyDesc);

        return classDesc;
    }

    public void testGenerateGapSource() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        classDesc.setName("com.example.dto.TestDto");

        String actual = target_.generateGapSource(classDesc);

        assertEquals(
            readResource(getClass(), "testGenerateGapSource.expected"), actual);
    }

    public void testGenerateBaseSource_Page() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        MethodDesc methodDesc = new MethodDesc("_get");
        methodDesc.getReturnTypeDesc().setDefaultType(String.class.getName());
        methodDesc.setBody("return \"redirect:/path/to/redirect.html\";");
        classDesc.setMethodDesc(methodDesc);

        String actual = target_.generateBaseSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateBaseSource_Page.expected"), actual);
    }

    public void testGenerateBaseSource_Dto() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        classDesc.setName("com.example.dto.TestDto");

        String actual = target_.generateBaseSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateBaseSource_Dto.expected"), actual);
    }

    public void testGenerateGapSource_Page() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        MethodDesc methodDesc = new MethodDesc("_get");
        methodDesc.getReturnTypeDesc().setDefaultType(String.class.getName());
        methodDesc.setBody("return \"redirect:/path/to/redirect.html\";");
        classDesc.setMethodDesc(methodDesc);

        String actual = target_.generateGapSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateGapSource_Page.expected"), actual);
    }
}
