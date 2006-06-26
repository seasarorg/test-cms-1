package org.seasar.cms.framework.zpt;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.PathTypePrefixHandler;

public class AnalyzerPathTypePrefixHandler extends PathTypePrefixHandler {

    public AnalyzerPathTypePrefixHandler(char pathExpDelim) {

        super(pathExpDelim);
    }

    protected Object getProperty(TemplateContext context,
        VariableResolver varResolver, String arg) {

        AnalyzerContext analyzerContext = toAnalyzerContext(context);
        SourceCreator sourceCreator = analyzerContext.getSourceCreator();

        if (DefaultRequestProcessor.ATTR_PAGE.equals(arg)) {
            return new DummyObject(analyzerContext.getPageClassName(), false);
        }

        String className = sourceCreator.getClassName(arg);
        if (className != null
            && className.startsWith(sourceCreator.getPagePackageName() + ".")) {
            return new DummyObject(className, false);
        }

        String dtoName = analyzerContext.getDtoName();
        if (arg.equals(dtoName)) {
            return new DummyObject(analyzerContext.getDtoClassName(), true);
        }

        Object value = super.getProperty(context, varResolver, arg);
        if (value == null) {
            // 極力attributeを解釈させるためのダミー。
            value = "0";
        }
        return value;
    }

    protected Object resolveSegment(TemplateContext context,
        VariableResolver varResolver, Object obj, String segment) {

        if (obj instanceof DummyObject) {
            DummyObject dummyObject = (DummyObject) obj;
            int mode = (dummyObject.isDto() ? (PropertyDesc.READ | PropertyDesc.WRITE)
                : PropertyDesc.READ);
            ClassDesc classDesc = toAnalyzerContext(context).getClassDesc(
                dummyObject.getClassName());
            classDesc.addProperty(segment, mode);
            return new Object[] { new DummyRepeatObject(segment) };
        } else {
            return super.resolveSegment(context, varResolver, obj, segment);
        }
    }

    AnalyzerContext toAnalyzerContext(TemplateContext context) {

        return (AnalyzerContext) context;
    }
}
