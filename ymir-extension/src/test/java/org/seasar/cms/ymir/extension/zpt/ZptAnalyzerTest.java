package org.seasar.cms.ymir.extension.zpt;

import static org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl.MOCK_REQUEST;
import static org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl.MOCK_RESPONSE;
import static org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl.MOCK_SERVLETCONTEXT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.PathMapping;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.impl.MethodDescImpl;
import org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl;
import org.seasar.cms.ymir.impl.ApplicationManagerImpl;
import org.seasar.cms.ymir.impl.PathMappingImpl;
import org.seasar.cms.ymir.mock.MockApplication;
import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.kvasir.util.el.VariableResolver;

import com.example.dto.SaruDto;

import net.skirnir.freyja.TemplateContext;

public class ZptAnalyzerTest extends TestCase {

    private static final String CLASSNAME = "com.example.web.IndexPage";

    private PathMapping[] mappings_ = new PathMapping[] {
        new PathMappingImpl("^/([^/]+)\\.(.+)$", "${1}Page", "${METHOD}", "",
                null, null),
        new PathMappingImpl("^/[^/]+/(.+)\\.(.+)$", "${1}Page", "${METHOD}",
                "", null, "_(.+)$"), };

    private ZptAnalyzer target_ = new ZptAnalyzer();

    private Map<String, ClassDesc> classDescMap_ = new HashMap<String, ClassDesc>();

    protected void setUp() throws Exception {

        target_ = new ZptAnalyzer() {
            @Override
            boolean isUsingFreyjaRenderClasses() {
                return false;
            }

            @Override
            AnalyzerTalTagEvaluator newAnalyzerTalTagEvaluator() {
                return new AnalyzerTalTagEvaluator() {
                    @Override
                    public TemplateContext newContext() {
                        TemplateContext context = super.newContext();
                        context.getVariableResolver().setVariable("saru",
                                new SaruDto());
                        return context;
                    }
                };
            }
        };
        SourceCreatorImpl creator = new SourceCreatorImpl() {

            @Override
            public MatchedPathMapping findMatchedPathMapping(String path,
                    String method) {
                for (int i = 0; i < mappings_.length; i++) {
                    VariableResolver resolver = mappings_[i]
                            .match(path, method);
                    if (resolver != null) {
                        return new MatchedPathMapping(mappings_[i], resolver);
                    }
                }
                return null;
            }

            @Override
            public String getComponentName(String path, String method) {
                MatchedPathMapping matched = findMatchedPathMapping(path,
                        method);
                if (matched == null) {
                    return null;
                } else {
                    return matched.getComponentName();
                }
            }

            @Override
            public String getClassName(String componentName) {
                if (componentName.endsWith("Page")) {
                    return "com.example.web."
                            + Character.toUpperCase(componentName.charAt(0))
                            + componentName.substring(1);
                } else {
                    return null;
                }
            }

            @Override
            public String getActionName(String path, String method) {
                MatchedPathMapping matched = findMatchedPathMapping(path,
                        method);
                if (matched == null) {
                    return null;
                } else {
                    return matched.getActionName();
                }
            }

            @Override
            public String getRootPackageName() {
                return "com.example";
            }
        };
        creator.setNamingConvention(new NamingConventionImpl());
        ApplicationManagerImpl applicationManager = new ApplicationManagerImpl();
        applicationManager.setBaseApplication(new MockApplication() {
            public Enumeration propertyNames() {
                return new Vector().elements();
            }
        });
        creator.setApplicationManager(applicationManager);
        target_.setSourceCreator(creator);
    }

    private void act(String methodName) {
        act(methodName, null);
    }

    private void act(final String methodName, String[] ignoreVariables) {

        target_.analyze(MOCK_SERVLETCONTEXT, MOCK_REQUEST, MOCK_RESPONSE,
                "/hoe", Request.METHOD_GET, classDescMap_, new Template() {
                    public InputStream getInputStream() throws IOException {
                        return getClass().getResourceAsStream(
                                "ZptAnalyzerTest_" + methodName + ".zpt");
                    }

                    public boolean exists() {
                        return true;
                    }

                    public String getName() {
                        return "ZptAnalyzerTest_" + methodName + ".zpt";
                    }

                    public OutputStream getOutputStream() throws IOException {
                        return null;
                    }

                    public String getPath() {
                        return "/hoe";
                    }

                    public long lastModified() {
                        return 0;
                    }
                }, CLASSNAME, ignoreVariables);
    }

    private ClassDesc getClassDesc(String name) {

        return (ClassDesc) classDescMap_.get(name);
    }

    public void testAnalyze1() throws Exception {

        act("testAnalyze1");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        assertNotNull(cd.getPropertyDesc("body"));
        assertNull("TemplateAnalyzerではリクエストメソッドのためのメソッド定義を生成しないこと", cd
                .getMethodDesc(new MethodDescImpl("GET")));
    }

    public void testAnalyze2() throws Exception {

        act("testAnalyze2");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("text");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        ClassDesc cd2 = getClassDesc("com.example.web.ActionPage");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("text");
        assertNotNull(pd2);
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze3() throws Exception {

        act("testAnalyze3");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entities");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertFalse(pd.getTypeDesc().isExplicit());
        assertEquals("com.example.dto.EntityDto[]", pd.getTypeDesc().getName());
        ClassDesc cd2 = getClassDesc("com.example.dto.EntityDto");
        assertNotNull(cd2);
        PropertyDesc pd2 = cd2.getPropertyDesc("content");
        assertNotNull(pd2);
        assertTrue(pd2.isReadable());
        assertTrue(pd2.isWritable());
    }

    public void testAnalyze4() throws Exception {

        act("testAnalyze4");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("strings");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertFalse(pd.getTypeDesc().isExplicit());
        assertEquals("プロパティを持たないリピート対象変数はStringの配列になること", "String[]", pd
                .getTypeDesc().getName());

        pd = cd.getPropertyDesc("entities");
        assertEquals("プロパティを持つリピート対象変数はDtoの配列になること",
                "com.example.dto.EntityDto[]", pd.getTypeDesc().getName());
        cd = getClassDesc("com.example.dto.EntityDto");
        assertNotNull("プロパティを持つリピート対象変数の型が生成されていること", cd);
        assertNotNull("Dto型がプロパティを持つこと", cd.getPropertyDesc("content"));
    }

    public void testAnalyze5() throws Exception {

        act("testAnalyze5");

        ClassDesc cd = getClassDesc("com.example.web.UpdatePage");
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("text");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isWritable());
        pd = cd.getPropertyDesc("select");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isWritable());
        pd = cd.getPropertyDesc("textarea");
        assertNotNull(pd);
        assertTrue(pd.isReadable());
        assertTrue(pd.isWritable());
        pd = cd.getPropertyDesc("file");
        assertNotNull(pd);
        assertEquals(FormFile.class.getName(), pd.getTypeDesc().getName());
        assertNotNull(cd.getPropertyDesc("button"));
        assertNotNull(cd.getPropertyDesc("image"));
        assertNotNull(cd.getPropertyDesc("submit"));

        assertNull(cd.getPropertyDesc("text2"));
        assertNull(cd.getPropertyDesc("select2"));
        assertNull(cd.getPropertyDesc("textarea2"));
        assertNull(cd.getPropertyDesc("file2"));
        assertNull(cd.getPropertyDesc("button2"));
        assertNull(cd.getPropertyDesc("image2"));
        assertNull(cd.getPropertyDesc("submit2"));

        MethodDesc md = cd.getMethodDesc(new MethodDescImpl("POST"));
        assertNotNull(md);
        assertEquals("void", md.getReturnTypeDesc().getName());
    }

    public void testAnalyze6() throws Exception {

        act("testAnalyze6");

        ClassDesc cd = getClassDesc("com.example.web.TestPage");
        assertNotNull("コンポーネント名が指定されている場合は対応するクラスのためのClassDescを生成すること", cd);
        assertNotNull(cd.getPropertyDesc("body"));
    }

    public void testAnalyze7() throws Exception {

        act("testAnalyze7");

        ClassDesc actual = getClassDesc("com.example.web.ActionPage");
        assertNotNull(actual);
        MethodDesc md = actual.getMethodDesc(new MethodDescImpl("GET"));
        assertNotNull(md);
        PropertyDesc pd = actual.getPropertyDesc("id");
        assertNotNull(pd);
    }

    public void testAnalyze8() throws Exception {

        act("testAnalyze8");

        ClassDesc actual = getClassDesc("com.example.web.ActionPage");
        assertNotNull(actual);
        MethodDesc md = actual.getMethodDesc(new MethodDescImpl("GET"));
        assertNotNull(md);
        PropertyDesc pd = actual.getPropertyDesc("id");
        assertNotNull(pd);
    }

    public void testAnalyze9() throws Exception {

        act("testAnalyze9");

        ClassDesc cd = getClassDesc("com.example.web.ActionPage");
        PropertyDesc pd = cd.getPropertyDesc("test");
        assertNotNull("formのパラメータに対応するプロパティが生成されること", pd);
        assertEquals("com.example.dto.TestDto", pd.getTypeDesc().getName());
        assertTrue(pd.isReadable());

        cd = getClassDesc("com.example.dto.TestDto");
        assertNotNull("formのパラメータを保持するためのDtoクラスが生成されること", cd);
        assertNotNull(cd.getPropertyDesc("name"));
        assertNotNull(cd.getPropertyDesc("body"));
    }

    public void testAnalyze10() throws Exception {

        act("testAnalyze10");

        ClassDesc cd = getClassDesc("com.example.dto.TestDto");
        PropertyDesc pd = cd.getPropertyDesc("file");
        assertEquals("ファイルパラメータについてはプロパティの型がFormFileになること", FormFile.class
                .getName(), pd.getTypeDesc().getName());
    }

    public void testAnalyze11() throws Exception {

        act("testAnalyze11");

        ClassDesc cd = getClassDesc("com.example.web.ActionPage");
        PropertyDesc pd = cd.getPropertyDesc("tests");
        assertTrue("フォームパラメータ名に添字指定がある場合は配列になること", pd.getTypeDesc().isArray());
        assertEquals("tests配列プロパティのDto型名は単数形になること", "com.example.dto.TestDto",
                pd.getTypeDesc().getClassDesc().getName());
    }

    public void testAnalyze12() throws Exception {

        act("testAnalyze12");

        ClassDesc cd = getClassDesc("com.example.dto.TestDto");
        PropertyDesc pd = cd.getPropertyDesc("files");
        assertEquals("添字指定があるファイルパラメータについてはプロパティの型がFormFileの配列になること",
                FormFile.class.getName() + "[]", pd.getTypeDesc().getName());
    }

    public void testAnalyze13() throws Exception {

        act("testAnalyze13");

        ClassDesc cd = getClassDesc("com.example.web.Image0Page");
        assertNotNull("page:指定のパラメータ置換が正しく行われ、その結果Pageクラスが生成されること", cd);
    }

    public void testAnalyze14() throws Exception {

        act("testAnalyze14");

        ClassDesc cd = getClassDesc(CLASSNAME);
        assertNotNull(cd);
        PropertyDesc pd = cd.getPropertyDesc("entity");
        assertEquals("プロパティを持つ変数はDtoの配列になること", "com.example.dto.EntityDto", pd
                .getTypeDesc().getName());
        cd = getClassDesc("com.example.dto.EntityDto");
        assertNotNull("プロパティを持つ変数の型が生成されていること", cd);
        assertNotNull("Dto型がプロパティを持つこと", cd.getPropertyDesc("content"));
    }

    public void testAnalyze15() throws Exception {

        act("testAnalyze15");

        assertNull(
                "組み込み変数など、VariableResolverから元々取得可能な変数に対応するClassDescは生成されないこと",
                getClassDesc("com.example.dto.RepeatDto"));
    }

    public void testAnalyze16() throws Exception {

        act("testAnalyze16");

        assertNotNull(
                "VariableResolverから取得可能であっても、クラスの属するパッケージが自動生成対象である場合はClassDescが生成されること",
                getClassDesc("com.example.dto.SaruDto"));
    }

    public void testAnalyze17() throws Exception {

        act("testAnalyze17");

        ClassDesc cd = getClassDesc("com.example.web.UpdatePage");
        assertNotNull(cd);
        assertNull(
                "dispatchingByRequestParameterがtrueであるようなPathMappingにactionのパスがマッチする場合はbuttonのnameに対応するプロパティのgetter/setterは生成されないこと",
                cd.getPropertyDesc("button"));
        assertNull(
                "dispatchingByRequestParameterがtrueであるようなPathMappingにactionのパスがマッチする場合はimageのnameに対応するプロパティのgetter/setterは生成されないこと",
                cd.getPropertyDesc("image"));
        assertNull(
                "dispatchingByRequestParameterがtrueであるようなPathMappingにactionのパスがマッチする場合はsubmitのnameに対応するプロパティのgetter/setterは生成されないこと",
                cd.getPropertyDesc("submit"));
        assertNotNull(
                "dispatchingByRequestParameterがtrueである場合でも、デフォルトのアクションメソッドは生成されること",
                cd.getMethodDesc(new MethodDescImpl("POST")));
        assertNotNull(
                "dispatchingByRequestParameterがtrueであるようなPathMappingにactionのパスがマッチする場合はbuttonのnameに対応するアクションメソッドが生成されること",
                cd.getMethodDesc(new MethodDescImpl("POST_button")));
        assertNotNull(
                "dispatchingByRequestParameterがtrueであるようなPathMappingにactionのパスがマッチする場合はimageのnameに対応するアクションメソッドが生成されること",
                cd.getMethodDesc(new MethodDescImpl("POST_image")));
        assertNotNull(
                "dispatchingByRequestParameterがtrueであるようなPathMappingにactionのパスがマッチする場合はsubmitのnameに対応するアクションメソッドが生成されること",
                cd.getMethodDesc(new MethodDescImpl("POST_submit")));
        assertNull("nameが実行時に決まるようなタグは無視されること", cd
                .getMethodDesc(new MethodDescImpl("POST_submit2")));
    }

    public void testAnalyze18() throws Exception {

        act("testAnalyze18");

        ClassDesc cd = getClassDesc(CLASSNAME).getPropertyDesc("entry")
                .getTypeDesc().getClassDesc();
        assertEquals("パスが3段以上でも正しくClassDescを生成できること",
                "com.example.dto.BodyDto", cd.getPropertyDesc("body")
                        .getTypeDesc().getClassDesc().getName());
    }

    public void testAnalyze19() throws Exception {

        act("testAnalyze19");

        assertNull("余計なDtoが生成されないこと",
                getClassDesc("com.example.dto.CommentsDto"));
    }

    public void testAnalyze20() throws Exception {

        act("testAnalyze20");

        PropertyDesc actual = getClassDesc(CLASSNAME)
                .getPropertyDesc("enabled");
        assertNotNull("tal:conditionの式については生成するプロパティの型がbooleanになること", actual);
        assertEquals("boolean", actual.getTypeDesc().getName());
    }

    public void testAnalyze21() throws Exception {

        act("testAnalyze21", new String[] { "self" });

        assertNull("無視するように指定した変数が正しく無視されること", getClassDesc(CLASSNAME));
    }

    public void testAnalyze22() throws Exception {

        act("testAnalyze22");

        ClassDesc cd = getClassDesc("com.example.web.ActionPage");
        PropertyDesc pd = cd.getPropertyDesc("check");
        assertTrue("同一のnameを持つパラメータが複数存在する場合は配列になること", pd.getTypeDesc()
                .isArray());
    }

    public void testAnalyze23() throws Exception {

        act("testAnalyze23");

        ClassDesc cd = getClassDesc("com.example.web.ActionPage");
        PropertyDesc pd = cd.getPropertyDesc("radio");
        assertFalse("同一のnameを持つパラメータがradio buttonである場合は配列にならないこと", pd
                .getTypeDesc().isArray());
    }

    public void testAnalyze24_配列に対するlength呼び出しがあっても正しく自動生成されること()
            throws Exception {

        act("testAnalyze24");

        ClassDesc cd = getClassDesc(CLASSNAME);
        PropertyDesc pd = cd.getPropertyDesc("results");
        assertTrue(pd.getTypeDesc().isArray());
        assertEquals("com.example.dto.ResultDto", pd.getTypeDesc()
                .getClassDesc().getName());
        assertNull(getClassDesc("com.example.dto.ResultDto").getPropertyDesc(
                "length"));
    }

    public void testAnalyze25_notesはYmir内蔵のNotesクラスと解釈されること() throws Exception {

        act("testAnalyze25");

        assertNull(getClassDesc("com.example.dto.NotesDto"));
    }
}
