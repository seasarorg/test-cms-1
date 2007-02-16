package org.seasar.cms.ymir.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.seasar.cms.ymir.HttpServletResponseFilter;
import org.seasar.cms.ymir.Updater;
import org.seasar.kvasir.util.MimeUtils;
import org.seasar.kvasir.util.io.IOUtils;

public class UpdaterResponseFilter extends HttpServletResponseWrapper implements
        HttpServletResponseFilter {

    private HttpServletRequest request_;

    private Updater[] updaters_;

    private String type_;

    private boolean underDevelopment_;

    private ByteArrayOutputStream outputStream_;

    private StringWriter writer_;

    private PrintWriter printWriter_;

    public UpdaterResponseFilter(HttpServletRequest request,
            HttpServletResponse response, Updater[] updaters,
            boolean underDevelopment) {
        super(response);
        request_ = request;
        updaters_ = updaters;
        underDevelopment_ = underDevelopment;
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
            return getResponse().getOutputStream();
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
            return getResponse().getWriter();
        }
    }

    public void setContentType(String type) {
        type_ = type;
        getResponse().setContentType(type);
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
        IOUtils.writeString(getResponse().getOutputStream(), response,
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
        IOUtils.writeBytes(getResponse().getOutputStream(), response);
    }
}
