package org.seasar.cms.ymir.extension.creator.action.impl;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDescSet;
import org.seasar.cms.ymir.extension.creator.impl.PathMetaDataImpl;
import org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImplTestBase;
import org.seasar.framework.util.ResourceUtil;

public class UpdateClassesActionTest extends SourceCreatorImplTestBase {

    private UpdateClassesAction target_;

    protected void setUp() throws Exception {

        super.setUp();
        target_ = new UpdateClassesAction(getSourceCreator());
    }

    @Override
    protected File getSourceDir() {
        return new File(ResourceUtil.getBuildDir(getClass()).getParentFile(),
                "src");
    }

    public void testShouldUpdate() throws Exception {

        File sourceDir = clean(getSourceDir());

        PathMetaDataImpl pathMetaData = new PathMetaDataImpl(null, null, false,
                null, null, null, null, getSourceCreator().getSourceFile(
                        "com.example.web.TestPage"), getSourceCreator()
                        .getTemplate("/test.html"));
        getSourceCreator().getSourceCreatorProperties().clear();
        assertTrue("ソースファイルが存在しない場合は最初だけtrueになること", target_
                .shouldUpdate(pathMetaData));
        assertFalse("ソースファイルが存在しない場合は最初だけtrueになること", target_
                .shouldUpdate(pathMetaData));

        Map<String, ClassDesc> classDescMap = new LinkedHashMap<String, ClassDesc>();
        getSourceCreator().gatherClassDescs(
                classDescMap,
                new PathMetaDataImpl("/test.html", Request.METHOD_GET, false,
                        "testPage", "com.example.web.TestPage", null, null,
                        null, getSourceCreator().getTemplate("/test.html")),
                null);
        ClassDesc[] classDescs = (ClassDesc[]) classDescMap.values().toArray(
                new ClassDesc[0]);
        ClassDescSet classDescSet = new ClassDescSet(classDescs);
        for (int i = classDescs.length - 1; i >= 0; i--) {
            getSourceCreator().mergeWithExistentClass(classDescs[i], true);
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

        assertFalse(target_.shouldUpdate(pathMetaData));
    }
}
