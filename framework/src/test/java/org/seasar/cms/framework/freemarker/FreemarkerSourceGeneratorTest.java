package org.seasar.cms.framework.freemarker;

import org.seasar.cms.framework.FrameworkTestCase;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;

public class FreemarkerSourceGeneratorTest extends FrameworkTestCase {

    private FreemarkerSourceGenerator generator_ = new FreemarkerSourceGenerator();

    private ClassDesc prepareClassDesc() {

        ClassDesc classDesc = new ClassDesc("com.example.page.TestPage");
        classDesc.setSuperclassName("com.example.page.SuperPage");
        PropertyDesc propertyDesc = new PropertyDesc("param1");
        propertyDesc.setDefaultType("boolean");
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
        propertyDesc.setType("java.lang.Integer[]");
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

    public void testGenerateBaseSource_Page() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        classDesc.setKind(ClassDesc.KIND_PAGE);
        MethodDesc methodDesc = new MethodDesc("_get");
        methodDesc.setDefaultReturnType(String.class.getName());
        methodDesc.setBody("return \"redirect:/path/to/redirect.html\";");
        classDesc.setMethodDesc(methodDesc);

        String actual = generator_.generateBaseSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateBaseSource_Page.expected"), actual);
    }

    public void testGenerateBaseSource_Dto() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        classDesc.setKind(ClassDesc.KIND_DTO);

        String actual = generator_.generateBaseSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateBaseSource_Dto.expected"), actual);
    }

    public void testGenerateGapSource_Page() throws Exception {

        ClassDesc classDesc = prepareClassDesc();
        classDesc.setKind(ClassDesc.KIND_PAGE);
        MethodDesc methodDesc = new MethodDesc("_get");
        methodDesc.setDefaultReturnType(String.class.getName());
        methodDesc.setBody("return \"redirect:/path/to/redirect.html\";");
        classDesc.setMethodDesc(methodDesc);

        String actual = generator_.generateGapSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateGapSource_Page.expected"), actual);
    }
}
