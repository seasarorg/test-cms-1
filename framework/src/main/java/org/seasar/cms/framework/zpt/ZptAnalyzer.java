package org.seasar.cms.framework.zpt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.TemplateAnalyzer;
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
            public TemplateContext newContext() {
                return new AnalyzerContext();
            }
        }, new ServletTalesExpressionEvaluator() {
            public HttpServletRequest getRequest(VariableResolver varResolver) {
                return new MockHttpServletRequestImpl(
                    new MockServletContextImpl("/") {
                        private static final long serialVersionUID = -4251298552610359164L;

                        public String getServletContextName() {
                            return "";
                        }
                    }, "/");
            }

            public HttpServletResponse getResponse(VariableResolver varResolver) {
                return new MockHttpServletResponseImpl(
                    new MockHttpServletRequestImpl(new MockServletContextImpl(
                        "/"), "/"));
            }
        }.addTypePrefix("path", new AnalyzerPathTypePrefixHandler('/')
            .addPathResolver(new BeanPathResolver()), true));

    private SourceCreator sourceCreator_;

    public void analyze(String method, Map classDescriptorMap,
        InputStream inputStream, String encoding, String className) {

        AnalyzerContext context = (AnalyzerContext) evaluator_.newContext();
        context.setSourceCreator(sourceCreator_);
        context.setMethod(method);
        context.setClassDescriptorMap(classDescriptorMap);
        context.setPageClassName(className);
        try {
            evaluator_.evaluate(context, new InputStreamReader(inputStream,
                encoding));
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IllegalSyntaxException) {
                // TODO 文法エラーがあったのでスキップする旨
                // 通知しよう。
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
}
