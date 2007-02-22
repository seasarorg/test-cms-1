package org.seasar.cms.ymir.extension.zpt;

import static net.skirnir.freyja.zpt.tales.TalesExpressionEvaluator.TYPE_NOT;
import static net.skirnir.freyja.zpt.webapp.ServletTalesExpressionEvaluator.TYPE_PAGE;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.extension.Globals;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.TemplateAnalyzer;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;

import net.skirnir.freyja.ExpressionEvaluator;
import net.skirnir.freyja.IllegalSyntaxException;
import net.skirnir.freyja.TagEvaluator;
import net.skirnir.freyja.TagEvaluatorWrapper;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.zpt.tales.TalesExpressionEvaluator;
import net.skirnir.freyja.zpt.webapp.PageTypePrefixHandler;

public class ZptAnalyzer implements TemplateAnalyzer {

    private TemplateEvaluator evaluator_;

    private SourceCreator sourceCreator_;

    private Zpt zpt_;

    public ZptAnalyzer() {

        setZpt(new DefaultZpt());
    }

    public void analyze(ServletContext servletContext,
            HttpServletRequest request, HttpServletResponse response,
            String path, String method, Map<String, ClassDesc> classDescMap,
            Template template, String className, String[] ignoreVariables) {

        path = zpt_.getTemplatePathResolver().resolve(path, request);
        AnalyzerContext context = (AnalyzerContext) evaluator_.newContext();
        context.setIgnoreVariables(ignoreVariables);
        zpt_.buildTemplateContext(context, servletContext, request, response,
                Locale.getDefault(), path);
        context.setTemplateSet(new AnalyzerTemplateSet(evaluator_, context
                .getTemplateSet(), sourceCreator_, zpt_
                .getTemplatePathResolver()));
        context.setSourceCreator(sourceCreator_);
        context.setMethod(method);
        context.setClassDescMap(classDescMap);
        context.setPageClassName(className);
        context.setUsingFreyjaRenderClasses(isUsingFreyjaRenderClasses());

        InputStream inputStream = null;
        try {
            inputStream = template.getInputStream();
            evaluator_.evaluate(context, new InputStreamReader(inputStream,
                    sourceCreator_.getEncoding()));
            context.close();
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IllegalSyntaxException) {
                // TODO 文法エラーがあったのでスキップする旨
                // 通知しよう。
                ex.printStackTrace();
            } else {
                throw ex;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    boolean isUsingFreyjaRenderClasses() {

        return "true".equals(sourceCreator_.getApplication().getProperty(
                Globals.APPKEY_SOURCECREATOR_USEFREYJARENDERCLASSES));
    }

    public void setSourceCreator(SourceCreator sourceCreator) {

        sourceCreator_ = sourceCreator;
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
    @Binding(bindingType = BindingType.NONE)
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
            TalesExpressionEvaluator evaluator = ((TalesExpressionEvaluator) expressionEvaluator);
            evaluator.addTypePrefix(TYPE_NOT,
                    new AnalyzerNotTypePrefixHandler()).addTypePrefix(
                    TYPE_PAGE,
                    new AnalyzerPageTypePrefixHandler(
                            (PageTypePrefixHandler) evaluator
                                    .getTypePrefixHandler(TYPE_PAGE)));
            evaluator.addPathResolver(new AnalyzerPathResolver());
        }
        evaluator_ = new TemplateEvaluator(tagEvaluator, expressionEvaluator);
    }

    AnalyzerTalTagEvaluator newAnalyzerTalTagEvaluator() {

        return new AnalyzerTalTagEvaluator();
    }

    public void setZpt(Zpt zpt) {
        zpt_ = zpt;
        setTemplateEvaluator(zpt_.getTemplateEvaluator());
    }
}
