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

    private boolean asNoCache_;

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

        return asString();
    }

    public String asString() {

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
                String encodedKey;
                try {
                    encodedKey = URLEncoder.encode(key, parameterEncoding_);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
                for (int i = 0; i < values.length; i++) {
                    sb.append(delim).append(encodedKey).append("=");
                    try {
                        sb.append(URLEncoder.encode(values[i],
                                parameterEncoding_));
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                    delim = "&";
                }
            }
            if (asNoCache_) {
                sb.append(delim).append(getUniqueKey());
                delim = "&";
            }
        }
        return sb.toString();
    }

    String getUniqueKey() {
        return String.valueOf(System.currentTimeMillis());
    }

    public String getTrunk() {

        return trunk_;
    }

    public Path setTrunk(String trunk) {

        trunk_ = trunk;
        return this;
    }

    public Map getParameterMap() {

        return parameterMap_;
    }

    public Path setParameterMap(Map parameterMap) {

        parameterMap_ = parameterMap;
        return this;
    }

    public Path setParameter(String name, String value) {

        return setParameter(name, new String[] { value });
    }

    public Path setParameter(String name, String[] values) {

        parameterMap_.put(name, values);
        return this;
    }

    public Path addParameter(String name, String value) {

        if (value == null) {
            return this;
        }

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
        return this;
    }

    public Path removeParameter(String name) {

        parameterMap_.remove(name);
        return this;
    }

    public Path clearParameters() {

        parameterMap_.clear();
        return this;
    }

    public String getParameterEncoding() {

        return parameterEncoding_;
    }

    public Path setParameterEncoding(String parameterEncoding) {

        parameterEncoding_ = parameterEncoding;
        return this;
    }

    public boolean isAsNoCache() {
        return asNoCache_;
    }

    public Path setAsNoCache(boolean asNoCache) {
        asNoCache_ = asNoCache;
        return this;
    }
}
