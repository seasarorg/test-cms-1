package org.seasar.cms.ymir.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.HttpServletResponseFilter;
import org.seasar.cms.ymir.Updater;
import org.seasar.kvasir.util.MimeUtils;
import org.seasar.kvasir.util.io.IOUtils;

public class UpdaterResponseFilter implements HttpServletResponseFilter {

    private HttpServletRequest request_;

    private HttpServletResponse response_;

    private Updater[] updaters_;

    private String type_;

    private boolean underDevelopment_;

    private ByteArrayOutputStream outputStream_;

    private StringWriter writer_;

    private PrintWriter printWriter_;

    public UpdaterResponseFilter(HttpServletRequest request,
            HttpServletResponse response, Updater[] updaters,
            boolean underDevelopment) {
        request_ = request;
        response_ = response;
        updaters_ = updaters;
        underDevelopment_ = underDevelopment;
    }

    public void addCookie(Cookie cookie) {
        response_.addCookie(cookie);
    }

    public void addDateHeader(String name, long date) {
        response_.addDateHeader(name, date);
    }

    public void addHeader(String name, String value) {
        response_.addHeader(name, value);
    }

    public void addIntHeader(String name, int value) {
        response_.addIntHeader(name, value);
    }

    public boolean containsHeader(String name) {
        return response_.containsHeader(name);
    }

    /**
     * @deprecated
     */
    public String encodeRedirectUrl(String url) {
        return response_.encodeRedirectUrl(url);
    }

    public String encodeRedirectURL(String url) {
        return response_.encodeRedirectURL(url);
    }

    /**
     * @deprecated
     */
    public String encodeUrl(String url) {
        return response_.encodeUrl(url);
    }

    public String encodeURL(String url) {
        return response_.encodeURL(url);
    }

    public void flushBuffer() throws IOException {
        response_.flushBuffer();
    }

    public int getBufferSize() {
        return response_.getBufferSize();
    }

    public String getCharacterEncoding() {
        return response_.getCharacterEncoding();
    }

    public String getContentType() {
        return response_.getContentType();
    }

    public Locale getLocale() {
        return response_.getLocale();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if (shouldBuffering()) {
            outputStream_ = new ByteArrayOutputStream();
            return new ServletOutputStream() {
                public void write(int b) throws IOException {
                    outputStream_.write(b);
                }
            };
        } else {
            return response_.getOutputStream();
        }
    }

    boolean shouldBuffering() {
        return (underDevelopment_ && type_ != null && type_.startsWith("text/"));
    }

    public PrintWriter getWriter() throws IOException {
        if (shouldBuffering()) {
            writer_ = new StringWriter();
            printWriter_ = new PrintWriter(writer_);
            return printWriter_;
        } else {
            return response_.getWriter();
        }
    }

    public boolean isCommitted() {
        return response_.isCommitted();
    }

    public void reset() {
        response_.reset();
    }

    public void resetBuffer() {
        response_.resetBuffer();
    }

    public void sendError(int sc, String msg) throws IOException {
        response_.sendError(sc, msg);
    }

    public void sendError(int sc) throws IOException {
        response_.sendError(sc);
    }

    public void sendRedirect(String location) throws IOException {
        response_.sendRedirect(location);
    }

    public void setBufferSize(int size) {
        response_.setBufferSize(size);
    }

    public void setCharacterEncoding(String charset) {
        response_.setCharacterEncoding(charset);
    }

    public void setContentLength(int len) {
        response_.setContentLength(len);
    }

    public void setContentType(String type) {
        type_ = type;
        response_.setContentType(type);
    }

    public void setDateHeader(String name, long date) {
        response_.setDateHeader(name, date);
    }

    public void setHeader(String name, String value) {
        response_.setHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        response_.setIntHeader(name, value);
    }

    public void setLocale(Locale loc) {
        response_.setLocale(loc);
    }

    /**
     * @deprecated
     */
    public void setStatus(int sc, String sm) {
        response_.setStatus(sc, sm);
    }

    public void setStatus(int sc) {
        response_.setStatus(sc);
    }

    public void commit() throws IOException {
        if (shouldBuffering()) {
            String response;
            if (outputStream_ != null) {
                String charset = MimeUtils.getCharset(type_);
                if (charset == null) {
                    charset = "ISO-8859-1";
                }
                try {
                    outputStream_.flush();
                } catch (IOException ex) {
                    throw new RuntimeException("Can't happen!", ex);
                }
                byte[] bytes = outputStream_.toByteArray();
                try {
                    response = new String(bytes, charset);
                } catch (UnsupportedEncodingException ex) {
                    commit(bytes);
                    return;
                }
            } else if (printWriter_ != null) {
                printWriter_.flush();
                response = writer_.toString();
            } else {
                return;
            }
            commit(filterResponse(response));
        }
    }

    String filterResponse(String response) {
        for (int i = 0; i < updaters_.length; i++) {
            response = updaters_[i].filterResponse(response);
        }
        return response;
    }

    void commit(String response) throws IOException {
        IOUtils.writeString(response_.getOutputStream(), response,
                getResponseCharset(), false);
    }

    String getResponseCharset() {
        String charset = MimeUtils.getCharset(type_);
        if (charset == null) {
            charset = "ISO-8859-1";
        }
        return charset;
    }

    void commit(byte[] response) throws IOException {
        IOUtils.writeBytes(response_.getOutputStream(), response);
    }
}
