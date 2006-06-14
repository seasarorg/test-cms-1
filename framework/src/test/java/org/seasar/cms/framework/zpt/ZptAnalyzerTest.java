package org.seasar.cms.framework.zpt;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;

public class ZptAnalyzerTest extends TestCase {

    private static final String CLASSNAME = "com.example.page.Page";

    private ZptAnalyzer analyzer_ = new ZptAnalyzer();

    private Map classDescriptorMap_ = new HashMap();

    protected void setUp() throws Exception {

        analyzer_ = new ZptAnalyzer();
        SourceCreatorImpl creator = new SourceCreatorImpl() {
            public String getComponentName(String path) {
                return path;
            }

            public String getClassName(String componentName) {
                int slash = componentName.lastIndexOf('/');
                if (slash >= 0) {
                    componentName = componentName.substring(slash + 1);
                }
                int dot = componentName.lastIndexOf('.');
                if (dot >= 0) {
                    componentName = componentName.substring(0, dot);
                }
                return "com.example.page."
                    + Character.toUpperCase(componentName.charAt(0))
                    + componentName.substring(1) + "Page";
            }
        };
        creator.setDtoPackageName("com.example.dto");
        analyzer_.setSourceCreator(creator);
    }

    private void act(String methodName) {

        analyzer_.analyze(classDescriptorMap_, getClass().getResourceAsStream(
            "ZptAnalyzerTest_" + methodName + ".zpt"), "UTF-8", CLASSNAME);
    }

    private ClassDesc getClassDescriptor(String name) {

        return (ClassDesc) classDescriptorMap_.get(name);
    }

    public void testAnalyze1() throws Exception {

        act("testAnalyze1");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        assertNotNull(cd.getPropertyDesc("body"));
    }

    public void testAnalyze2() throws Exception {

        act("testAnalyze2");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("text");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        ClassDesc cd2 = getClassDescriptor("com.example.page.ActionPage");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("text");
        assertNotNull(pd2);
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze3() throws Exception {

        act("testAnalyze3");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entities");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isArray());
        assertNull(pd.getType());
        assertEquals("com.example.dto.EntityDto", pd.getDefaultType());
        ClassDesc cd2 = getClassDescriptor("com.example.dto.EntityDto");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("content");
        assertNotNull(pd2);
        assertTrue(pd2.isReadable());
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze4() throws Exception {

        act("testAnalyze4");

        ClassDesc cd = getClassDescriptor(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entities");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isArray());
        assertNull(pd.getType());
    }
}
