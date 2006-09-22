package org.seasar.cms.webdavjp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class RequestURIDecodingFilter implements Filter {
    private FilterConfig config_;

    private int debug_ = 0;

    private boolean useServletPathAsPathInfo_;

    private boolean disallowNullPathInfo_;

    private boolean destinationUrlDecode_;

    private boolean destinationAbsolutePath_;

    private String nativeEncoding_;

    private String urlEncoding_;

    private String[] nativeEncodingNames_;

    private String[] nativeEncodings_;

    private String[] urlEncodingNames_;

    private String[] urlEncodings_;

    /*
     * public scope methods
     */

    public void init(FilterConfig config) {
        config_ = config;

        String debug = config_.getInitParameter("debug");
        if (debug != null) {
            try {
                debug_ = Integer.parseInt(debug);
            } catch (NumberFormatException ex) {
                ;
            }
        }

        useServletPathAsPathInfo_ = "true".equalsIgnoreCase(config_
                .getInitParameter("useServletPathAsPathInfo"));

        disallowNullPathInfo_ = "true".equalsIgnoreCase(config_
                .getInitParameter("disallowNullPathInfo"));

        destinationUrlDecode_ = "true".equalsIgnoreCase(config_
                .getInitParameter("destination.urlDecode"));

        destinationAbsolutePath_ = "true".equalsIgnoreCase(config_
                .getInitParameter("destination.absolutePath"));

        List nativeEncodingNameList = new ArrayList();
        List urlEncodingNameList = new ArrayList();

        Enumeration enm = config_.getInitParameterNames();
        while (enm.hasMoreElements()) {
            String name = (String) enm.nextElement();
            if (name.startsWith("nativeEncoding.")) {
                nativeEncodingNameList.add(name
                        .substring(15/*="nativeEncoding.".length()*/));
            } else if (name.startsWith("urlEncoding.")) {
                urlEncodingNameList.add(name
                        .substring(12/*="urlEncoding.".length()*/));
            }
        }

        nativeEncodingNames_ = (String[]) nativeEncodingNameList
                .toArray(new String[0]);
        Arrays.sort(nativeEncodingNames_, new StringLengthComparator());
        nativeEncodings_ = new String[nativeEncodingNames_.length];
        for (int i = 0; i < nativeEncodingNames_.length; i++) {
            String name = nativeEncodingNames_[i];
            if (debug_ > 0) {
                System.out.println("nativeEncoding name: " + name);
            }
            nativeEncodings_[i] = config_.getInitParameter("nativeEncoding."
                    + name);
        }

        urlEncodingNames_ = (String[]) urlEncodingNameList
                .toArray(new String[0]);
        Arrays.sort(urlEncodingNames_, new StringLengthComparator());
        urlEncodings_ = new String[urlEncodingNames_.length];
        for (int i = 0; i < urlEncodingNames_.length; i++) {
            String name = urlEncodingNames_[i];
            if (debug_ > 0) {
                System.out.println("urlEncoding name: " + name);
            }
            urlEncodings_[i] = config_.getInitParameter("urlEncoding." + name);
        }

        nativeEncoding_ = config_.getInitParameter("nativeEncoding");
        if (nativeEncoding_ == null) {
            nativeEncoding_ = "JISAutoDetect";
        }
        urlEncoding_ = config_.getInitParameter("urlEncoding");
        if (urlEncoding_ == null) {
            urlEncoding_ = "JISAutoDetect";
        }
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        String userAgent = request.getHeader("User-Agent");
        String nativeEncoding = null;
        String urlEncoding = null;

        if (debug_ > 0) {
            System.out.println("UserAgent: " + userAgent);
        }

        if (userAgent != null) {
            for (int i = 0; i < nativeEncodingNames_.length; i++) {
                if (userAgent.indexOf(nativeEncodingNames_[i]) >= 0) {
                    if (debug_ > 0) {
                        System.out.println("Matched (nativeEncoding): "
                                + nativeEncodingNames_[i]);
                        System.out.println("Value = " + nativeEncodings_[i]);
                    }
                    nativeEncoding = nativeEncodings_[i];
                    break;
                }
            }
            for (int i = 0; i < urlEncodingNames_.length; i++) {
                if (userAgent.indexOf(urlEncodingNames_[i]) >= 0) {
                    if (debug_ > 0) {
                        System.out.println("Matched (urlEncoding): "
                                + urlEncodingNames_[i]);
                        System.out.println("Value = " + urlEncodings_[i]);
                    }
                    urlEncoding = urlEncodings_[i];
                    break;
                }
            }
        }
        if (nativeEncoding == null) {
            nativeEncoding = nativeEncoding_;
        }
        if (urlEncoding == null) {
            urlEncoding = urlEncoding_;
        }

        RequestURIDecodingRequest r = new RequestURIDecodingRequest(this,
                request, nativeEncoding, urlEncoding);

        chain.doFilter(r, res);
    }

    /*
     * protected scope methods
     */

    protected int getDebug() {
        return debug_;
    }

    protected boolean isUseServletPathAsPathInfo() {
        return useServletPathAsPathInfo_;
    }

    protected boolean isDisallowNullPathInfo() {
        return disallowNullPathInfo_;
    }

    protected boolean isDestinationUrlDecode() {
        return destinationUrlDecode_;
    }

    protected boolean isDestinationAbsolutePath() {
        return destinationAbsolutePath_;
    }

    /*
     * inner classes
     */

    private static class StringLengthComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int cmp = s2.length() - s1.length();
            if (cmp == 0) {
                cmp = s1.compareTo(s2);
            }
            return cmp;
        }
    }
}
