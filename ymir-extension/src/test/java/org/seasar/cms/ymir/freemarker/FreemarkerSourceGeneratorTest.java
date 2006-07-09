package org.seasar.cms.ymir.freemarker;

import org.seasar.cms.ymir.FrameworkTestCase;
import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.MethodDesc;
import org.seasar.cms.ymir.creator.MockSourceCreator;
import org.seasar.cms.ymir.creator.PropertyDesc;
import org.seasar.cms.ymir.creator.impl.BodyDescImpl;
import org.seasar.cms.ymir.creator.impl.ClassDescImpl;
import org.seasar.cms.ymir.creator.impl.MethodDescImpl;
import org.seasar.cms.ymir.creator.impl.PropertyDescImpl;

public class FreemarkerSourceGeneratorTest extends FrameworkTestCase {

    private FreemarkerSourceGenerator target_;

    protected void setUp() throws Exception {

        super.setUp();
        target_ = new FreemarkerSourceGenerator();
        target_.setSourceCreator(new MockSourceCreator() {

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
        });
    }

    private ClassDesc prepareClassDesc() {

        ClassDesc classDesc = new ClassDescImpl("com.example.page.TestPage");
        classDesc.setSuperclassName("com.example.page.SuperPage");
        PropertyDesc propertyDesc = new PropertyDescImpl("param1");
        propertyDesc.setTypeDesc("boolean");
        propertyDesc.setMode(PropertyDesc.READ);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDescImpl("param2");
        propertyDesc.setMode(PropertyDesc.WRITE);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDescImpl("param3");
        propertyDesc.setMode(PropertyDesc.READ | PropertyDesc.WRITE);
        classDesc.setPropertyDesc(propertyDesc);
        propertyDesc = new PropertyDescImpl("param4");
        propertyDesc.setMode(PropertyDesc.READ | PropertyDesc.WRITE);
        propertyDesc.setTypeDesc("java.lang.Integer[]", true);
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
        MethodDesc methodDesc = new MethodDescImpl("_get");
        methodDesc.setReturnTypeDesc(String.class.getName());
        methodDesc.setBodyDesc(new BodyDescImpl(
            "return \"redirect:/path/to/redirect.html\";"));
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
        MethodDesc methodDesc = new MethodDescImpl("_get");
        methodDesc.setReturnTypeDesc(String.class.getName());
        methodDesc.setBodyDesc(new BodyDescImpl(
            "return \"redirect:/path/to/redirect.html\";"));
        classDesc.setMethodDesc(methodDesc);

        String actual = target_.generateGapSource(classDesc);

        assertEquals(readResource(getClass(),
            "testGenerateGapSource_Page.expected"), actual);
    }

    public void testGenerateSource_BodyDesc() throws Exception {

        String actual = target_.generateSource(new BodyDescImpl("test"));

        assertEquals("test", actual);
    }
}
