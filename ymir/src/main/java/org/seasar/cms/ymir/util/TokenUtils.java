package org.seasar.cms.ymir.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.cms.ymir.Globals;

public class TokenUtils {
    public static final String KEY_TOKEN = Globals.IDPREFIX + "token";

    protected TokenUtils() {
    }

    public static String generateToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            byte[] id = session.getId().getBytes();
            byte[] now = new Long(System.currentTimeMillis()).toString()
                    .getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(id);
            md.update(now);
            return toHex(md.digest());
        } catch (IllegalStateException ex) {
            return null;
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }

    public static String getToken(HttpServletRequest request) {
        return getToken(request, KEY_TOKEN);
    }

    public static String getToken(HttpServletRequest request, String tokenKey) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        } else {
            return (String) session.getAttribute(tokenKey);
        }
    }

    public static boolean isTokenValid(HttpServletRequest request) {
        return isTokenValid(request, KEY_TOKEN);
    }

    public static boolean isTokenValid(HttpServletRequest request,
            String tokenKey) {
        return isTokenValid(request, tokenKey, false);
    }

    public static boolean isTokenValid(HttpServletRequest request, boolean reset) {
        return isTokenValid(request, KEY_TOKEN, reset);
    }

    public static boolean isTokenValid(HttpServletRequest request,
            String tokenKey, boolean reset) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        synchronized (session.getId().intern()) {
            Object saved = session.getAttribute(tokenKey);
            if (saved == null) {
                return false;
            }
            if (reset) {
                session.removeAttribute(tokenKey);
            }

            String token = request.getParameter(tokenKey);
            if (token == null) {
                return false;
            }

            return saved.equals(token);
        }
    }

    public static void resetToken(HttpServletRequest request) {
        resetToken(request, KEY_TOKEN);
    }

    public static void resetToken(HttpServletRequest request, String tokenKey) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(tokenKey);
        }
    }

    public static void saveToken(HttpServletRequest request) {
        saveToken(request, KEY_TOKEN);
    }

    public static void saveToken(HttpServletRequest request, String tokenKey) {
        saveToken(request, tokenKey, true);
    }

    public static void saveToken(HttpServletRequest request, boolean force) {
        saveToken(request, KEY_TOKEN, force);
    }

    public static void saveToken(HttpServletRequest request, String tokenKey,
            boolean force) {
        HttpSession session = request.getSession(false);
        if (!force && session != null && session.getAttribute(tokenKey) != null) {
            return;
        }

        if (session == null) {
            session = request.getSession();
        }

        String token = generateToken(request);
        if (token != null) {
            session.setAttribute(tokenKey, token);
        }
    }

    static String toHex(byte[] buffer) {
        StringBuffer sb = new StringBuffer();
        String s = null;
        for (int i = 0; i < buffer.length; i++) {
            s = Integer.toHexString((int) buffer[i] & 0xff);
            if (s.length() < 2) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
