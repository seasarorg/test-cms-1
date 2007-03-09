package org.seasar.cms.ymir.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.cms.ymir.Globals;

public class LocaleUtils {

    protected LocaleUtils() {
    }

    /**
     * 指定されたrequestを元にロケールを決定します。
     * <p>ロケールの決定方法は次の通りです：</p>
     * <ol>
     * <li>session中に
     * {@link Globals#LOCALE_KEY}というキーでにバインドされているLocale</li>
     * <li><code>request.getLocale()の返り値</code></li>
     * </ol>
     * <p><code>request</code>としてnullを指定した場合、
     * システムのデフォルトロケールを返します。
     * </p>
     *
     * @param request リクエストオブジェクト。nullを指定することもできます。
     * @return Localeオブジェクト。
     */
    public static Locale findLocale(HttpServletRequest request) {

        if (request != null) {
            Locale locale = null;
            HttpSession session = request.getSession(false);
            if (session != null) {
                locale = (Locale) session.getAttribute(Globals.ATTR_LOCALE);
            }
            if (locale == null) {
                locale = request.getLocale();
            }
            return locale;
        } else {
            return Locale.getDefault();
        }
    }

    public static void setLocale(HttpServletRequest request, Locale locale) {

        request.getSession().setAttribute(Globals.ATTR_LOCALE, locale);
    }

    public static void removeLocale(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(Globals.ATTR_LOCALE);
        }
    }
}
