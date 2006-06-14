package org.seasar.cms.framework.zpt;

import org.seasar.cms.framework.creator.PropertyDesc;
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

        if (DefaultRequestProcessor.ATTR_PAGE.equals(arg)) {
            return new DummyObject(toAnalyzerContext(context)
                .getPageClassName(), false);
        }
        AnalyzerContext analyzerContext = toAnalyzerContext(context);
        String dtoName = analyzerContext.getDtoName();
        if (arg.equals(dtoName)) {
            return new DummyObject(analyzerContext.getDtoClassName(), true);
        }

        return super.getProperty(context, varResolver, arg);
    }

    protected Object resolveSegment(TemplateContext context,
        VariableResolver varResolver, Object obj, String segment) {

        if (obj instanceof DummyObject) {
            DummyObject dummyObject = (DummyObject) obj;
            int mode = (dummyObject.isDto() ? (PropertyDesc.READ | PropertyDesc.WRITE)
                : PropertyDesc.READ);
            toAnalyzerContext(context).getClassDescriptor(
                dummyObject.getClassName()).addProperty(segment, mode);
            return new Object[] { new DummyRepeatObject(segment) };
        } else {
            return super.resolveSegment(context, varResolver, obj, segment);
        }
    }

    AnalyzerContext toAnalyzerContext(TemplateContext context) {

        return (AnalyzerContext) context;
    }
}
