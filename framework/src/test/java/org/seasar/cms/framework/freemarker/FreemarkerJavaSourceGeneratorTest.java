package org.seasar.cms.framework.freemarker;

import org.seasar.cms.framework.generator.ClassDesc;
import org.seasar.cms.framework.generator.PropertyDesc;
import org.seasar.kvasir.util.io.IOUtils;

import junit.framework.TestCase;

public class FreemarkerJavaSourceGeneratorTest extends TestCase {

    private FreemarkerJavaSourceGenerator generator_ = new FreemarkerJavaSourceGenerator();

    private String readResource(String name) {

        return IOUtils.readString(getClass().getResourceAsStream(
            "FreemarkerJavaSourceGeneratorTest_" + name), "UTF-8", false);
    }

    private ClassDesc prepareClassDesc() {

        ClassDesc classDesc = new ClassDesc("com.example.page.TestPage");
        PropertyDesc propertyDesc = new PropertyDesc("param1");
        propertyDesc.setMode(PropertyDesc.READ);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDesc("param2");
        propertyDesc.setMode(PropertyDesc.WRITE);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDesc("param3");
        propertyDesc.setMode(PropertyDesc.READ | PropertyDesc.WRITE);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDesc("param4");
        propertyDesc.setMode(PropertyDesc.READ | PropertyDesc.WRITE
            | PropertyDesc.ARRAY);
        propertyDesc.setType("java.lang.Integer");
        classDesc.setPropertyDesc(propertyDesc);

        return classDesc;
    }

    public void testGeneratePageSource() throws Exception {

        ClassDesc classDesc = prepareClassDesc();

        String actual = generator_.generatePageSource(classDesc);

        assertEquals(readResource("testGeneratePageSource.expected"), actual);
    }

    public void testGeneratePageBaseSource() throws Exception {

        ClassDesc classDesc = prepareClassDesc();

        String actual = generator_.generatePageBaseSource(classDesc);

        assertEquals(readResource("testGeneratePageBaseSource.expected"),
            actual);
    }
}
