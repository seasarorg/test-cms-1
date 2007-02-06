package org.seasar.cms.webdavjp;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RequestURIDecodingRequest extends HttpServletRequestWrapper {
    public static final String ATTR_FORWARD_PATH_INFO = "javax.servlet.forward.path_info";

    public static final String ATTR_FORWARD_SERVLET_PATH = "javax.servlet.forward.servlet_path";

    public static final String ATTR_FORWARD_REQUEST_URI = "javax.servlet.forward.request_uri";

    private RequestURIDecodingFilter filter_;

    private String nativeEncoding_;

    private String urlEncoding_;

    private String requestURI_;

    private String requestURL_;

    private String pathInfo_;

    private String servletPath_;

    private String destination_;

    /*
     * constructors
     */

    public RequestURIDecodingRequest(RequestURIDecodingFilter filter,
            HttpServletRequest request, String nativeEncoding,
            String urlEncoding) {
        super(request);

        filter_ = filter;

        int debug = filter_.getDebug();

        if (debug > 0) {
            System.out.println("Method: " + request.getMethod());
            System.out.println("requestURI: " + request.getRequestURI());
            System.out.println("requestURL: " + request.getRequestURL());
            System.out.println("pathInfo: " + request.getPathInfo());
            System.out.println("servletPath: " + request.getServletPath());
            System.out.println("Destination: "
                    + request.getHeader("Destination"));
            System.out.println("nativeEncoding: " + nativeEncoding);
            System.out.println("urlEncoding: " + urlEncoding);
        }

        nativeEncoding_ = nativeEncoding;
        urlEncoding_ = urlEncoding;
        requestURI_ = decode(request.getRequestURI());
        requestURL_ = decode(request.getRequestURL().toString());

        String contextPath = request.getContextPath();
        String relativeURI = requestURI_.substring(contextPath.length());
        relativeURI = URLDecode(relativeURI);
        validate(relativeURI);

        destination_ = request.getHeader("Destination");
        if (destination_ != null) {
            destination_ = URLDecode(decode(destination_));
            if (!filter_.isDestinationUrlDecode()) {
                // FIXME - ここでは、元々URLエンコードされていた
                // 文字のうちASCII文字に関しては再エンコードすべきだが、
                // これは非常に困難なので今はそうなっていない。
                destination_ = RequestURIDecodingUtils.reencode(destination_);
            }
            if (filter_.isDestinationAbsolutePath()) {
                // Tomcat4.1.29, Tomcat4.1.30のWebdavServlet不具合を回避
                // するため。
                int protocol = destination_.indexOf("://");
                if (protocol >= 0) {
                    int slash = destination_
                            .indexOf('/', protocol + 3/*= "://".length() */);
                    if (slash < 0) {
                        destination_ = "/";
                    } else {
                        destination_ = destination_.substring(slash);
                    }
                }
            }
        }

        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            // pathInfoがない場合はservletPathに日本語が入っていることを
            // 想定する。
            servletPath_ = relativeURI;
            pathInfo_ = null;

            if (filter_.isUseServletPathAsPathInfo()) {
                pathInfo_ = servletPath_;
                servletPath_ = "";
            }
        } else {
            // pathInfoがある場合はservletPathに日本語が入っていることを
            // 想定しない。
            servletPath_ = servletPath;
            pathInfo_ = relativeURI.substring(servletPath.length());

            if (filter_.isUseServletPathAsPathInfo()) {
                pathInfo_ = servletPath_ + pathInfo_;
                servletPath_ = "";
            }
        }

        if (pathInfo_ == null && filter_.isDisallowNullPathInfo()) {
            pathInfo_ = "";
        }

        if (debug > 0) {
            System.out.println("-> requestURI: " + getRequestURI());
            System.out.println("-> requestURL: " + getRequestURL());
            System.out.println("-> pathInfo: " + getPathInfo());
            System.out.println("-> servletPath: " + getServletPath());
            System.out.println("-> Destination: " + getHeader("Destination"));
        }
    }

    /*
     * static methods
     */

    protected static byte convertHexDigit(byte b) {
        if (b >= '0' && b <= '9') {
            return (byte) (b - '0');
        } else if (b >= 'a' && b <= 'f') {
            return (byte) (b - 'a' + 10);
        } else if (b >= 'A' && b <= 'F') {
            return (byte) (b - 'A' + 10);
        } else {
            // XXX - こういうものなのかな？
            return 0;
        }
    }

    /*
     * public scope methods
     */

    public String getRequestURI() {
        if (isForwared()) {
            return getHttpRequest().getRequestURI();
        } else {
            return requestURI_;
        }
    }

    HttpServletRequest getHttpRequest() {
        return (HttpServletRequest) getRequest();
    }

    boolean isForwared() {
        return (getHttpRequest().getAttribute(ATTR_FORWARD_REQUEST_URI) != null);
    }

    public StringBuffer getRequestURL() {
        if (isForwared()) {
            return getHttpRequest().getRequestURL();
        } else {
            return new StringBuffer(requestURL_);
        }
    }

    public String getPathInfo() {
        if (isForwared()) {
            return getHttpRequest().getPathInfo();
        } else {
            return pathInfo_;
        }
    }

    public String getServletPath() {
        if (isForwared()) {
            return getHttpRequest().getServletPath();
        } else {
            return servletPath_;
        }
    }

    public String getHeader(String name) {
        if ("destination".equalsIgnoreCase(name)) {
            return destination_;
        } else {
            return getHttpRequest().getHeader(name);
        }
    }

    public Enumeration getHeaders(String name) {
        if ("destination".equalsIgnoreCase(name)) {
            Vector vector = new Vector();
            if (destination_ != null) {
                vector.add(destination_);
            }
            return vector.elements();
        } else {
            return getHttpRequest().getHeaders(name);
        }
    }

    /*
     * protected scope methods
     */

    protected String decode(String orig) {
        try {
            return decode(orig, nativeEncoding_);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding: "
                    + nativeEncoding_);
        }
    }

    protected String decode(String orig, String encoding)
            throws UnsupportedEncodingException {
        if (orig == null) {
            return orig;
        }

        char[] chars = orig.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }

        // Tomcat4ではリクエストURIに含まれる \（0x5c）を /（0x2f）に
        // 変換してしまうが、
        // MS932の場合は2バイトコードの2バイト目に0x5cを含みうるので
        // 文字化けしてしまうことになる。そこで、MS932の場合は変換されて
        // しまった \ を元に戻す処理を行なう。
        if (encoding.equalsIgnoreCase("MS932")) {
            int stat = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                char c = (char) (b < 0 ? b + 256 : b);
                if (stat == 0) {
                    if (c >= 129 && c < 160 || c >= 224 && c < 240) {
                        stat = 1;
                    }
                } else if (stat == 1) {
                    // シフトJISの第2バイト。
                    if (c == 0x2f) {
                        bytes[i] = (byte) 0x5c;
                    }
                    stat = 0;
                }
            }
        }

        return new String(bytes, encoding);
    }

    protected String URLDecode(String str) {
        try {
            return URLDecode(str, urlEncoding_);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding: " + urlEncoding_);
        }
    }

    protected String URLDecode(String str, String encoding)
            throws UnsupportedEncodingException {
        if (str == null) {
            return str;
        }

        byte[] bytes = str.getBytes(encoding);
        int pos = 0;
        int wpos = 0;
        while (pos < bytes.length) {
            byte b = bytes[pos++];
            /*
             if (b == '+') {
             bytes[wpos] = (byte)' ';
             } else
             */
            if (b == '%') {
                if (pos + 1 >= bytes.length) {
                    throw new IllegalArgumentException(
                            "Illegal % character detected: " + str);
                }
                bytes[wpos] = (byte) ((convertHexDigit(bytes[pos]) << 4) + convertHexDigit(bytes[pos + 1]));
                pos += 2;
            } else {
                bytes[wpos] = b;
            }
            wpos++;
        }
        return new String(bytes, 0, wpos, encoding);
    }

    protected String URLEncode(String str, String encoding)
            throws UnsupportedEncodingException {
        if (str == null) {
            return str;
        }

        StringBuffer sb = new StringBuffer();
        byte[] bytes = str.getBytes(encoding);
        for (int i = 0; i < bytes.length; i++) {
            int ch;
            if (bytes[i] >= 0) {
                ch = bytes[i];
            } else {
                ch = bytes[i] + 256;
            }
            if (ch >= 0x30 && ch <= 0x39 || ch >= 0x41 && ch <= 0x5a
                    || ch >= 0x61 && ch <= 0x7a || ch == 0x2a || ch == 0x2d
                    || ch == 0x2e || ch == 0x5f) {
                sb.append((char) ch);
            } else if (ch == 0x20) {
                sb.append('+');
            } else {
                sb.append('%');
                String hex = Integer.toHexString(ch);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
        }

        return sb.toString();
    }

    protected void validate(String relativeURI) {
        int n = relativeURI.length();
        for (int i = 0; i < n; i++) {
            char ch = relativeURI.charAt(i);
            if (ch < '\u0020' || ch == '\u007f') {
                throw new IllegalArgumentException("Illegal URI: "
                        + relativeURI);
            }
        }
    }
}
