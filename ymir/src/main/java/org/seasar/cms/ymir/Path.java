package org.seasar.cms.ymir;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Path {
    private String trunk_;

    private Map parameterMap_;

    private String parameterEncoding_;

    public Path() {

        this(null);
    }

    public Path(String path) {

        this(path, new LinkedHashMap());
    }

    public Path(String path, Map parameterMap) {

        this(path, parameterMap, "UTF-8");
    }

    public Path(String path, Map parameterMap, String parameterEncoding) {

        parameterMap_ = parameterMap;
        parameterEncoding_ = parameterEncoding;
        analyze(path);
    }

    void analyze(String path) {

        if (path == null) {
            return;
        }

        int question = path.indexOf('?');
        if (question < 0) {
            trunk_ = path;
            return;
        }
        trunk_ = path.substring(0, question);
        int pre = question + 1;
        int idx;
        while ((idx = path.indexOf('&', pre)) >= 0) {
            addEncodedParameter(path.substring(pre, idx));
            pre = idx + 1;
        }
        if (pre < path.length()) {
            addEncodedParameter(path.substring(pre));
        }
    }

    void addEncodedParameter(String encodedParam) {
        int equal = encodedParam.indexOf('=');
        if (equal >= 0) {
            try {
                addParameter(URLDecoder.decode(
                        encodedParam.substring(0, equal), parameterEncoding_),
                        URLDecoder.decode(encodedParam.substring(equal + 1),
                                parameterEncoding_));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(trunk_);
        if (parameterMap_ != null) {
            String delim = "?";
            for (Iterator itr = parameterMap_.entrySet().iterator(); itr
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) itr.next();
                String key = (String) entry.getKey();
                String[] values = (String[]) entry.getValue();
                if (key == null || values == null || values.length == 0) {
                    continue;
                }
                sb.append(delim);
                delim = "&";
                String encodedKey;
                try {
                    encodedKey = URLEncoder.encode(key, parameterEncoding_);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
                for (int i = 0; i < values.length; i++) {
                    sb.append(encodedKey);
                    sb.append("=");
                    try {
                        sb.append(URLEncoder.encode(values[i],
                                parameterEncoding_));
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return sb.toString();
    }

    public String getTrunk() {

        return trunk_;
    }

    public void setTrunk(String trunk) {

        trunk_ = trunk;
    }

    public Map getParameterMap() {

        return parameterMap_;
    }

    public void setParameterMap(Map parameterMap) {

        parameterMap_ = parameterMap;
    }

    public void setParameter(String name, String value) {

        setParameter(name, new String[] { value });
    }

    public void setParameter(String name, String[] values) {

        parameterMap_.put(name, values);
    }

    public void addParameter(String name, String value) {

        String[] values = (String[]) parameterMap_.get(name);
        String[] newValues;
        if (values == null) {
            newValues = new String[] { value };
        } else {
            newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
        }
        parameterMap_.put(name, newValues);
    }

    public void removeParameter(String name) {

        parameterMap_.remove(name);
    }

    public void clearParameters() {

        parameterMap_.clear();
    }

    public String getParameterEncoding() {

        return parameterEncoding_;
    }

    public void setParameterEncoding(String parameterEncoding) {

        parameterEncoding_ = parameterEncoding;
    }
}
