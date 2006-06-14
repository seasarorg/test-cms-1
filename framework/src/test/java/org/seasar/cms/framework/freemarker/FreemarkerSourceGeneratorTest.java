package org.seasar.cms.framework.freemarker;

import org.seasar.cms.framework.FrameworkTestCase;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;

public class FreemarkerSourceGeneratorTest extends FrameworkTestCase {

    private FreemarkerSourceGenerator generator_ = new FreemarkerSourceGenerator();

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

        assertEquals(
            readResource(getClass(), "testGeneratePageSource.expected"), actual);
    }

    public void testGeneratePageBaseSource() throws Exception {

        ClassDesc classDesc = prepareClassDesc();

        String actual = generator_.generatePageBaseSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGeneratePageBaseSource.expected"), actual);
    }
}
