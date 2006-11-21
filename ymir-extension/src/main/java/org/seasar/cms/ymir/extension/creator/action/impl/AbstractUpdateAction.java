package org.seasar.cms.ymir.extension.creator.action.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.action.UpdateAction;
import org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl;

abstract public class AbstractUpdateAction implements UpdateAction {

    public static final String PARAM_SUBTASK = SourceCreator.PARAM_PREFIX
            + "subTask";

    private SourceCreator sourceCreator_;

    public AbstractUpdateAction(SourceCreator sourceCreator) {

        sourceCreator_ = sourceCreator;
    }

    public SourceCreator getSourceCreator() {

        return sourceCreator_;
    }

    protected String getSuffix(String name) {

        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return "";
        } else {
            return name.substring(dot);
        }
    }

    protected String quote(String string) {

        StringBuffer sb = new StringBuffer();
        sb.append('"');
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '"' || ch == '\\') {
                sb.append('\\');
            }
            sb.append(ch);
        }
        sb.append('"');
        return sb.toString();
    }

    protected Parameter[] getParameters(Request request) {

        List<Parameter> list = new ArrayList<Parameter>();
        for (Iterator itr = request.getParameterMap().entrySet().iterator(); itr
                .hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            String name = (String) entry.getKey();
            if (name.startsWith(SourceCreatorImpl.PARAM_PREFIX)) {
                continue;
            }
            String[] values = (String[]) entry.getValue();
            for (int i = 0; i < values.length; i++) {
                list.add(new Parameter(name, values[i]));
            }
        }
        return list.toArray(new Parameter[0]);
    }

    public static class Parameter {
        private String name_;

        private String value_;

        public Parameter(String name, String value) {
            name_ = name;
            value_ = value;
        }

        public String getName() {
            return name_;
        }

        public String getValue() {
            return value_;
        }
    }
}
