package org.seasar.cms.framework.zpt;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.PathTypePrefixHandler;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.impl.ClassDescImpl;

public class AnalyzerPathTypePrefixHandler extends PathTypePrefixHandler {

    public AnalyzerPathTypePrefixHandler(char pathExpDelim) {

        super(pathExpDelim);
    }

    protected Object getProperty(TemplateContext context,
        VariableResolver varResolver, String arg) {

        AnalyzerContext analyzerContext = toAnalyzerContext(context);

        ClassDescImpl classDesc = analyzerContext.getTemporaryClassDesc(arg);
        if (!ClassDesc.KIND_DTO.equals(classDesc.getKind())
            || !classDesc.isEmpty()) {
            return classDesc;
        } else {
            Object value = super.getProperty(context, varResolver, arg);
            if (value == null) {
                // 極力attributeを解釈させるためのダミー。
                value = "0";
            }
            return value;
        }
    }

    protected Object resolveSegment(TemplateContext context,
        VariableResolver varResolver, Object obj, String segment) {

        AnalyzerContext analyzerContext = toAnalyzerContext(context);
        if (obj instanceof ClassDescImpl) {
            ClassDescImpl classDesc = (ClassDescImpl) obj;
            PropertyDesc propertyDesc = analyzerContext.getPropertyDesc(
                classDesc, segment, '/', PropertyDesc.READ);
            return propertyDesc.getTypeDesc();
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
