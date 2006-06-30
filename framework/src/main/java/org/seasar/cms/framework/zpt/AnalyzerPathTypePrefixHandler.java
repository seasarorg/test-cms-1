package org.seasar.cms.framework.zpt;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.PathTypePrefixHandler;

import org.seasar.cms.framework.creator.PropertyHolder;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

public class AnalyzerPathTypePrefixHandler extends PathTypePrefixHandler {

    public AnalyzerPathTypePrefixHandler(char pathExpDelim) {

        super(pathExpDelim);
    }

    protected Object getProperty(TemplateContext context,
        VariableResolver varResolver, String arg) {

        AnalyzerContext analyzerContext = toAnalyzerContext(context);
        SourceCreator sourceCreator = analyzerContext.getSourceCreator();

        if (DefaultRequestProcessor.ATTR_PAGE.equals(arg)) {
            // 現在のPageオブジェクト。
            return analyzerContext.getPropertyHolder(arg);
        }

        String className = sourceCreator.getClassName(arg);
        if (className != null
            && className.startsWith(sourceCreator.getPagePackageName() + ".")) {
            // 他のPageオブジェクト。
            return analyzerContext.getPropertyHolder(arg);
        }

        PropertyHolder propertyHolder = analyzerContext.getPropertyHolder(arg,
            false);
        if (propertyHolder != null) {
            return propertyHolder;
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

        AnalyzerContext analyzerContext = toAnalyzerContext(context);

        if (obj instanceof PropertyHolder) {
            PropertyHolder propertyHolder = (PropertyHolder) obj;
            PropertyHolder.Property property = new PropertyHolder.Property(
                segment);
            propertyHolder.addProperty(property);
            return new PropertyHolder.Property[] { property };
        } else if (obj instanceof PropertyHolder.Property[]) {
            PropertyHolder.Property prevProperty = ((PropertyHolder.Property[]) obj)[0];
            PropertyHolder propertyHolder = analyzerContext
                .getPropertyHolder(prevProperty.getName());
            prevProperty.setType(propertyHolder);

            PropertyHolder.Property property = new PropertyHolder.Property(
                segment);
            propertyHolder.addProperty(property);
            return new PropertyHolder.Property[] { property };
        } else {
            return super.resolveSegment(context, varResolver, obj, segment);
        }
    }

    AnalyzerContext toAnalyzerContext(TemplateContext context) {

        return (AnalyzerContext) context;
    }
}
