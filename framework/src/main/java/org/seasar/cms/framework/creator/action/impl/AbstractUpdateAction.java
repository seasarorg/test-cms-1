package org.seasar.cms.framework.creator.action.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.DescValidator;
import org.seasar.cms.framework.creator.action.UpdateAction;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;

abstract public class AbstractUpdateAction implements UpdateAction {

    public static final String PARAM_SUBTASK = SourceCreatorImpl.PARAM_PREFIX
        + "subTask";

    public static final String PARAM_METHOD = SourceCreatorImpl.PARAM_PREFIX
        + "method";

    private SourceCreatorImpl sourceCreator_;

    public AbstractUpdateAction(SourceCreatorImpl sourceCreator) {

        sourceCreator_ = sourceCreator;
    }

    public SourceCreatorImpl getSourceCreator() {

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

    protected boolean writeSourceFile(ClassDesc classDesc) {

        if (!DescValidator.isValid(classDesc, getSourceCreator())) {
            return false;
        }

        writeString(getSourceCreator().getSourceGenerator().generateBaseSource(
            classDesc), getSourceCreator().getSourceFile(
            classDesc.getName() + "Base"));

        // gap側のクラスは存在しない場合のみ生成する。
        File sourceFile = getSourceCreator().getSourceFile(classDesc.getName());
        if (!sourceFile.exists()) {
            writeString(getSourceCreator().getSourceGenerator()
                .generateGapSource(classDesc), sourceFile);
        }

        return true;
    }

    protected void writeString(String string, File file) {

        if (string == null) {
            return;
        }

        file.getParentFile().mkdirs();

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                os, getSourceCreator().getEncoding()));
            writer.write(string);
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    protected Parameter[] getParameters(Request request) {

        List list = new ArrayList();
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
        return (Parameter[]) list.toArray(new Parameter[0]);
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
