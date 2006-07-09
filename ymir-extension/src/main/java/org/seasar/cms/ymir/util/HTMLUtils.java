package org.seasar.cms.ymir.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

public class HTMLUtils {

    private HTMLUtils() {
    }

    public static String filterLines(String text) {

        return filterLines(text, false);
    }

    public static String filterLines(String text, boolean toNbsp) {

        if (text == null) {
            return null;
        }

        boolean endsWithSeparator = false;
        int n = text.length();
        if (n > 0) {
            char ch = text.charAt(n - 1);
            if (ch == '\r' || ch == '\n') {
                endsWithSeparator = true;
            }
        }

        String separator = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        String delim = "";

        StringReader sr = new StringReader(text);
        BufferedReader in = new BufferedReader(sr);
        try {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(delim);
                if (toNbsp) {
                    delim = "<br />" + separator;
                } else {
                    delim = separator;
                }
                sb.append(filter(line, toNbsp));
            }
        } catch (IOException ex) {
            // Can't happen!
            throw new RuntimeException(ex);
        }
        if (endsWithSeparator) {
            sb.append(delim);
        }

        return sb.toString();
    }

    public static String filter(String line) {

        return filter(line, false);
    }

    public static String filter(String line, boolean toNbsp) {

        if (line == null) {
            return line;
        }

        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(line, "&<> \"'", true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String encode;
            if (token.equals("&")) {
                encode = "&amp;";
            } else if (token.equals("<")) {
                encode = "&lt;";
            } else if (token.equals(">")) {
                encode = "&gt;";
            } else if (token.equals(" ")) {
                if (toNbsp) {
                    encode = "&nbsp;";
                } else {
                    encode = " ";
                }
            } else if (token.equals("\"")) {
                encode = "&quot;";
            } else if (token.equals("'")) {
                encode = "&#39;";
            } else {
                encode = token;
            }
            sb.append(encode);
        }

        return sb.toString();
    }

    public static String defilter(String html) {

        if (html == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        defilter(sb, html);
        return sb.toString();
    }

    public static void defilter(StringBuffer sb, String html) {

        String htmlLowerCase = html.toLowerCase();

        int pre = 0;
        int idx;
        while ((idx = html.indexOf("&", pre)) >= 0) {
            sb.append(html.substring(pre, idx));

            int semi = html.indexOf(";", idx + 1);
            if (semi < 0) {
                pre = idx;
                break;
            }

            String tkn = htmlLowerCase.substring(idx + 1, semi);
            String decode;
            if (tkn.equals("amp")) {
                decode = "&";
            } else if (tkn.equals("lt")) {
                decode = "<";
            } else if (tkn.equals("gt")) {
                decode = ">";
            } else if (tkn.equals("nbsp")) {
                decode = " ";
            } else if (tkn.equals("quot")) {
                decode = "\"";
            } else if (tkn.equals("apos")) {
                decode = "'";
            } else if (tkn.startsWith("#")) {
                String code = tkn.substring(1);
                int radix = 10;
                if (code.startsWith("x")) {
                    code = code.substring(1);
                    radix = 16;
                }
                try {
                    decode = String.valueOf((char) Integer
                        .parseInt(code, radix));
                } catch (Throwable t) {
                    decode = html.substring(idx, semi + 1);
                }
            } else {
                decode = html.substring(idx, semi + 1);
            }

            sb.append(decode);
            pre = semi + 1;
        }
        sb.append(html.substring(pre));

        return;
    }
}
