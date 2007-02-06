package org.seasar.cms.webdavjp;

import java.io.UnsupportedEncodingException;

public class RequestURIDecodingUtils {

    private static final char[] byteTable_ = new char[] { '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    protected RequestURIDecodingUtils() {
    }

    /**
     * 指定されたURL中の非ASCIIコードに関してのみ
     * URLエンコードを行ないます。
     * <p>このメソッドは{@link #reencode(String, String)}でエンコーディングとして
     * UTF-8を指定したのと同じです。
     * </p>
     *
     * @param url エンコードするURL。
     * @return エンコードしたURL。
     */
    public static String reencode(String url) {
        return reencode(url, "UTF-8");
    }

    /**
     * 指定されたURL中の非ASCIIコードに関してのみ
     * URLエンコードを行ないます。
     * <p>エンコード後の文字エンコーディングは<code>encoding</code>で指定されたものになります。</p>
     * <p>非ASCIIコード部分以外はエンコードされません。
     * 従って '%' 等はそのままになります。</p>
     *
     * @param url エンコードするURL。
     * @param encoding エンコーディング。
     * @return エンコードしたURL。
     */
    public static String reencode(String url, String encoding) {
        if (url == null) {
            return null;
        }

        try {
            StringBuffer sb = new StringBuffer();
            int n = url.length();
            for (int i = 0; i < n; i++) {
                char ch = url.charAt(i);
                if (ch > 0x7f) {
                    byte[] bytes = url.substring(i, i + 1).getBytes(encoding);
                    for (int j = 0; j < bytes.length; j++) {
                        int b = bytes[j];
                        if (b < 0) {
                            b += 256;
                        }
                        sb.append("%");
                        sb.append(byteTable_[b / 16]);
                        sb.append(byteTable_[b % 16]);
                    }
                } else {
                    sb.append(ch);
                }
            }

            return sb.toString();
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Can't happen!");
        }
    }
}
