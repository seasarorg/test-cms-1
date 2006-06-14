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

    public void testGenerateGapSource() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        classDesc.setKind(ClassDesc.KIND_DTO);

        String actual = generator_.generateGapSource(classDesc);

        assertEquals(
            readResource(getClass(), "testGenerateGapSource.expected"), actual);
    }

    public void testGenerateBaseSource() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        classDesc.setKind(ClassDesc.KIND_DTO);

        String actual = generator_.generateBaseSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateBaseSource.expected"), actual);
    }
}
