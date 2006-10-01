package org.seasar.cms.ymir.extension.zpt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.YmirVariableResolver;
import org.seasar.cms.ymir.extension.Globals;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.TemplateAnalyzer;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;

import net.skirnir.freyja.ExpressionEvaluator;
import net.skirnir.freyja.IllegalSyntaxException;
import net.skirnir.freyja.TagEvaluator;
import net.skirnir.freyja.TagEvaluatorWrapper;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.webapp.ServletVariableResolverFactory;
import net.skirnir.freyja.webapp.VariableResolverFactory;
import net.skirnir.freyja.zpt.MetalTagEvaluator;
import net.skirnir.freyja.zpt.TalTagEvaluator;
import net.skirnir.freyja.zpt.tales.BeanPathResolver;
import net.skirnir.freyja.zpt.tales.TalesExpressionEvaluator;
import net.skirnir.freyja.zpt.webapp.ServletTalesExpressionEvaluator;

public class ZptAnalyzer implements TemplateAnalyzer {

    private static final ServletContext MOCK_SERVLETCONTEXT = new MockServletContextImpl(
            "/") {
        private static final long serialVersionUID = 0;

        public String getServletContextName() {
            return "";
        }
    };

    private static final HttpServletRequest MOCK_REQUEST = new MockHttpServletRequestImpl(
            MOCK_SERVLETCONTEXT, "/");

    private static final HttpServletResponse MOCK_RESPONSE = new MockHttpServletResponseImpl(
            MOCK_REQUEST);

    private TemplateEvaluator evaluator_;

    private VariableResolverFactory vrf_;

    private SourceCreator sourceCreator_;

    private AnalyzerTemplateSet templateSet_;

    private TemplatePathNormalizer templatePathNormalizer_ = new DefaultTemplatePathNormalizer();

    public ZptAnalyzer() {

        setTemplateEvaluator(new TemplateEvaluator(new MetalTagEvaluator(
                new TalTagEvaluator()), new ServletTalesExpressionEvaluator()));
        setVariableResolverFactory(new ServletVariableResolverFactory());
    }

    public void analyze(String path, String method,
            Map<String, ClassDesc> classDescMap, InputStream inputStream,
            String encoding, String className) {

        templateSet_.setPathNormalizer(templatePathNormalizer_);
        AnalyzerContext context = (AnalyzerContext) evaluator_.newContext();
        context.setTemplateName(path);
        context.setTemplateSet(templateSet_);
        context.setSourceCreator(sourceCreator_);
        context.setPathNormalizer(templatePathNormalizer_);
        context.setMethod(method);
        context.setClassDescMap(classDescMap);
        context.setPageClassName(className);
        context.setUsingFreyjaRenderClasses(isUsingFreyjaRenderClasses());
        context.setVariableResolver(vrf_.newResolver(MOCK_REQUEST,
                MOCK_RESPONSE, MOCK_SERVLETCONTEXT, new YmirVariableResolver(
                        MOCK_REQUEST)));

        try {
            evaluator_.evaluate(context, new InputStreamReader(inputStream,
                    encoding));
            context.close();
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IllegalSyntaxException) {
                // TODO 文法エラーがあったのでスキップする旨
                // 通知しよう。
                ex.printStackTrace();
            } else {
                throw ex;
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    boolean isUsingFreyjaRenderClasses() {

        return "true".equals(sourceCreator_.getApplication().getProperty(
                Globals.APPKEY_SOURCECREATOR_USEFREYJARENDERCLASSES));
    }

    public void setSourceCreator(SourceCreator sourceCreator) {

        sourceCreator_ = sourceCreator;
        templateSet_ = new AnalyzerTemplateSet("UTF-8", evaluator_,
                sourceCreator);
    }

    public void setTemplatePathNormalizer(
            TemplatePathNormalizer templatePathNormalizer) {

        templatePathNormalizer_ = templatePathNormalizer;
    }

    /**
     * テンプレートの解析に用いるTemplateEvaluatorを設定します。
     * <p>指定されたTemplateEvaluatorオブジェクトは状態を変更されますので、
     * 外部で共用することは避けるべきです。
     * 必ず実際に使うTemplateEvaluatorオブジェクトの複製を渡すようにして下さい。
     * </p>
     *
     * @param templateEvaluator TemplateEvaluatorオブジェクト。
     * nullを指定することはできません。
     */
    public void setTemplateEvaluator(TemplateEvaluator templateEvaluator) {

        TagEvaluator tagEvaluator = templateEvaluator.getTagEvaluator();
        if (tagEvaluator instanceof TagEvaluatorWrapper) {
            ((TagEvaluatorWrapper) tagEvaluator)
                    .setTagEvaluator(newAnalyzerTalTagEvaluator());
        } else {
            tagEvaluator = newAnalyzerTalTagEvaluator();
        }
        ExpressionEvaluator expressionEvaluator = templateEvaluator
                .getExpressionEvaluator();
        if (expressionEvaluator instanceof TalesExpressionEvaluator) {
            ((TalesExpressionEvaluator) expressionEvaluator).addTypePrefix(
                    "not", new AnalyzerNotTypePrefixHandler()).addTypePrefix(
                    "path",
                    new AnalyzerPathTypePrefixHandler('/')
                            .addPathResolver(new BeanPathResolver()), true);
        }
        evaluator_ = new TemplateEvaluator(tagEvaluator, expressionEvaluator);
    }

    AnalyzerTalTagEvaluator newAnalyzerTalTagEvaluator() {

        return new AnalyzerTalTagEvaluator();
    }

    public void setVariableResolverFactory(VariableResolverFactory vrf) {

        vrf_ = vrf;
    }
}
