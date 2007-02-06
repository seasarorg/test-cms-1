package org.seasar.cms.ymir.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.HttpServletResponseFilter;
import org.seasar.cms.ymir.RedirectionPathResolver;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseProcessor;
import org.seasar.cms.ymir.Updater;
import org.seasar.cms.ymir.Ymir;

public class DefaultResponseProcessor implements ResponseProcessor {

    private static final int BUF_SIZE = 4096;

    private Ymir ymir_;

    private Updater[] updaters_ = new Updater[0];

    private RedirectionPathResolver redirectionPathResolver_ = new DefaultRedirectionPathResolver();

    public void setYmir(Ymir ymir) {

        ymir_ = ymir;
    }

    public void setUpdaters(Updater[] updaters) {

        updaters_ = updaters;
    }

    public HttpServletResponseFilter process(ServletContext context,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            Request request, Response response) throws IOException,
            ServletException {

        if (response.getStatus() != Response.STATUS_UNDEFINED) {
            httpResponse.setStatus(response.getStatus());
        }
        if (response.getContentType() != null) {
            httpResponse.setContentType(response.getContentType());
        }

        switch (response.getType()) {
        case Response.TYPE_PASSTHROUGH:
            return constructResponseFilter(httpRequest, httpResponse);

        case Response.TYPE_FORWARD:
            context.getRequestDispatcher(response.getPath()).forward(
                    httpRequest, httpResponse);
            return null;

        case Response.TYPE_REDIRECT:
            String resolved = redirectionPathResolver_.resolve(response
                    .getPath(), request);
            if (resolved == null) {
                throw new NullPointerException(
                        "Redirection path is null: may logic is wrong");
            }
            if (resolved.startsWith("/")) {
                // 内部パスの場合はエンコードする。
                resolved = httpResponse.encodeRedirectURL(resolved);
            }
            httpResponse.sendRedirect(resolved);
            return null;

        case Response.TYPE_SELF_CONTAINED:
            InputStream is = null;
            OutputStream os = null;
            try {
                is = response.getInputStream();
                os = httpResponse.getOutputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                BufferedOutputStream bos = new BufferedOutputStream(os);

                byte[] buf = new byte[BUF_SIZE];
                int len;
                while ((len = bis.read(buf)) >= 0) {
                    bos.write(buf, 0, len);
                }
                bos.flush();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        ;
                    }
                }
                // httpResponseのoutputStreamのクローズはservletContainerに
                // 任せて良い（と思う）。
            }
            return null;

        case Response.TYPE_VOID:
            return null;

        default:
            throw new RuntimeException("Unknown response type:"
                    + response.getType());
        }
    }

    HttpServletResponseFilter constructResponseFilter(
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return new UpdaterResponseFilter(httpRequest, httpResponse, updaters_,
                ymir_.isUnderDevelopment());
    }

    public void setRedirectionPathResolver(
            RedirectionPathResolver responsePathNormalizer) {
        redirectionPathResolver_ = responsePathNormalizer;
    }
}
