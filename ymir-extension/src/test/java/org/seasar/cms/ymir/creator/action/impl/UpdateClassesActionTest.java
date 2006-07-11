package org.seasar.cms.ymir.creator.action.impl;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.ClassDescSet;
import org.seasar.cms.ymir.creator.impl.PathMetaDataImpl;
import org.seasar.cms.ymir.creator.impl.SourceCreatorImplTestBase;
import org.seasar.framework.util.ResourceUtil;

public class UpdateClassesActionTest extends SourceCreatorImplTestBase {

    private UpdateClassesAction target_;

    protected void setUp() throws Exception {

        super.setUp();
        target_ = new UpdateClassesAction(getSourceCreator());
    }

    public void testShouldUpdate() throws Exception {

        File sourceDir = clean(new File(ResourceUtil.getBuildDir(getClass())
            .getParentFile(), "src"));
        getSourceCreator().setSourceDirectoryPath(sourceDir.getCanonicalPath());

        assertTrue(target_.shouldUpdate(new PathMetaDataImpl(null, null, false,
            null, null, null, null, getSourceCreator().getSourceFile(
                "com.example.web.TestPage"), getSourceCreator()
                .getTemplateFile("/test.html"))));

        Map<String, ClassDesc> classDescMap = new LinkedHashMap<String, ClassDesc>();
        getSourceCreator().gatherClassDescs(
            classDescMap,
            new PathMetaDataImpl("/test.html", Request.METHOD_GET, false,
                "testPage", "com.example.web.TestPage", null, null, null,
                getSourceCreator().getTemplateFile("/test.html")));
        ClassDesc[] classDescs = (ClassDesc[]) classDescMap.values().toArray(
            new ClassDesc[0]);
        ClassDescSet classDescSet = new ClassDescSet(classDescs);
        for (int i = classDescs.length - 1; i >= 0; i--) {
            classDescs[i].merge(getSourceCreator().getClassDesc(
                classDescs[i].getName()), true);
            getSourceCreator().writeSourceFile(classDescs[i], classDescSet);
        }

        assertTrue(new File(sourceDir, "com/example/web/TestPage.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/web/TestPageBase.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/dto/EntityDto.java")
            .exists());
        assertTrue(new File(sourceDir, "com/example/dto/EntityDtoBase.java")
            .exists());

        assertFalse(target_.shouldUpdate(new PathMetaDataImpl(null, null,
            false, null, null, null, null, getSourceCreator().getSourceFile(
                "com.example.web.TestPage"), getSourceCreator()
                .getTemplateFile("/test.html"))));
    }
}
