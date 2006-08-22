package org.seasar.cms.ymir.extension.zpt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.TemplateAnalyzer;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;

import net.skirnir.freyja.IllegalSyntaxException;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.MetalTagEvaluator;
import net.skirnir.freyja.zpt.tales.BeanPathResolver;
import net.skirnir.freyja.zpt.webapp.ServletTalesExpressionEvaluator;

public class ZptAnalyzer implements TemplateAnalyzer {

    private TemplateEvaluator evaluator_ = new TemplateEvaluator(
            new MetalTagEvaluator(new AnalyzerTalTagEvaluator()) {
                public String[] getSpecialTagPatternStrings() {
                    return new String[] { "form", "input", "select", "textarea" };
                }

                public TemplateContext newContext() {
                    return new AnalyzerContext();
                }
            }, new ServletTalesExpressionEvaluator() {
                public HttpServletRequest getRequest(
                        VariableResolver varResolver) {
                    return new MockHttpServletRequestImpl(
                            new MockServletContextImpl("/") {
                                private static final long serialVersionUID = -4251298552610359164L;

                                public String getServletContextName() {
                                    return "";
                                }
                            }, "/");
                }

                public HttpServletResponse getResponse(
                        VariableResolver varResolver) {
                    return new MockHttpServletResponseImpl(
                            new MockHttpServletRequestImpl(
                                    new MockServletContextImpl("/"), "/"));
                }

                public String getResponseEncoding() {
                    return "UTF-8";
                }

            }.addTypePrefix("path", new AnalyzerPathTypePrefixHandler('/')
                    .addPathResolver(new BeanPathResolver()), true));

    private SourceCreator sourceCreator_;

    private PathNormalizer pathNormalizer_ = new PathNormalizerImpl();

    public void analyze(String method, Map<String, ClassDesc> classDescMap,
            InputStream inputStream, String encoding, String className) {

        AnalyzerContext context = (AnalyzerContext) evaluator_.newContext();
        context.setSourceCreator(sourceCreator_);
        context.setPathNormalizer(pathNormalizer_);
        context.setMethod(method);
        context.setClassDescMap(classDescMap);
        context.setPageClassName(className);
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

    public void setSourceCreator(SourceCreator sourceCreator) {

        sourceCreator_ = sourceCreator;
    }

    public void setPathNormalizer(PathNormalizer pathNormalizer) {
        pathNormalizer_ = pathNormalizer;
    }
}
