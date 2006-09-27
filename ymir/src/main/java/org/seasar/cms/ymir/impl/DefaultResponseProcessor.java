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

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponsePathNormalizer;
import org.seasar.cms.ymir.ResponseProcessor;

public class DefaultResponseProcessor implements ResponseProcessor {

    private static final int BUF_SIZE = 4096;

    private ResponsePathNormalizer responsePathNormalizer_ = new DefaultResponsePathNormalizer();

    public boolean process(ServletContext context,
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
            return true;

        case Response.TYPE_FORWARD:
            context.getRequestDispatcher(response.getPath()).forward(
                    httpRequest, httpResponse);
            return false;

        case Response.TYPE_REDIRECT:
            httpResponse.sendRedirect(responsePathNormalizer_
                    .normalizeForRedirection(response.getPath(), request));
            return false;

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
            return false;

        case Response.TYPE_VOID:
            return false;

        default:
            throw new RuntimeException("Unknown response type:"
                    + response.getType());
        }
    }

    public void setResponsePathNormalizer(
            ResponsePathNormalizer responsePathNormalizer) {
        responsePathNormalizer_ = responsePathNormalizer;
    }
}
