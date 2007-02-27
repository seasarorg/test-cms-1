package org.seasar.cms.ymir.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author YOKOTA Takehiko
 */
public class ServletUtils {
    public static final String ATTR_INCLUDE_CONTEXT_PATH = "javax.servlet.include.context_path";

    public static final String ATTR_INCLUDE_PATH_INFO = "javax.servlet.include.path_info";

    public static final String ATTR_INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path";

    private ServletUtils() {
    }

    public static String getRequestContextPath(HttpServletRequest request) {
        return request.getContextPath();
    }

    public static String getRequestPath(HttpServletRequest request) {
        String path = getNativeRequestPath(request);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String getNativeRequestPath(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();
        sb.append(request.getServletPath());
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            sb.append(pathInfo);
        }
        return sb.toString();
    }

    public static String getContextPath(HttpServletRequest request) {
        String contextPath = getIncludeContextPath(request);
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        return contextPath;
    }

    public static String getPath(HttpServletRequest request) {
        String path = getNativePath(request);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String getNativePath(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();
        String servletPath = getIncludeServletPath(request);
        String pathInfo;
        if (servletPath != null) {
            pathInfo = getIncludePathInfo(request);
        } else {
            servletPath = request.getServletPath();
            pathInfo = request.getPathInfo();
        }
        sb.append(servletPath);
        if (pathInfo != null) {
            sb.append(pathInfo);
        }
        return sb.toString();
    }

    public static String getIncludeContextPath(HttpServletRequest request) {
        return (String) request.getAttribute(ATTR_INCLUDE_CONTEXT_PATH);
    }

    public static String getIncludePathInfo(HttpServletRequest request) {
        return (String) request.getAttribute(ATTR_INCLUDE_PATH_INFO);
    }

    public static String getIncludeServletPath(HttpServletRequest request) {
        return (String) request.getAttribute(ATTR_INCLUDE_SERVLET_PATH);
    }

    public static String constructURI(String path, Map paramMap, String encoding)
            throws UnsupportedEncodingException {
        if (paramMap == null) {
            return path;
        }

        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        StringBuffer sb = new StringBuffer(path);
        String delim = "?";
        Iterator itr = paramMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null) {
                continue;
            }
            String encodedKey = URLEncoder.encode(key, encoding);
            String[] values;
            if (value instanceof String[]) {
                values = (String[]) value;
            } else {
                values = new String[] { value.toString() };
            }
            for (int i = 0; i < values.length; i++) {
                sb.append(delim);
                delim = "&";
                sb.append(encodedKey);
                sb.append("=");
                sb.append(URLEncoder.encode(values[i], encoding));
            }
        }

        return sb.toString();
    }

    public static boolean isIncluded(HttpServletRequest request) {

        return (request.getAttribute(ATTR_INCLUDE_PATH_INFO) != null || request
                .getAttribute(ATTR_INCLUDE_SERVLET_PATH) != null);
    }

    public static void setNoCache(HttpServletResponse response) {

        if (!response.containsHeader("Cache-Control")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control",
                    "no-cache,no-store,must-revalidate");
            response.setDateHeader("Expires", 1);
        }
    }
}
