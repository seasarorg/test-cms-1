package org.seasar.cms.webdavjp;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class RequestURIDecodingResponse extends HttpServletResponseWrapper {

    private String nativeEncoding_;

    private String urlEncoding_;

    public RequestURIDecodingResponse(RequestURIDecodingFilter filter,
            HttpServletResponse response, String nativeEncoding,
            String urlEncoding) {
        super(response);
        nativeEncoding_ = nativeEncoding;
        urlEncoding_ = urlEncoding;
    }

    public void sendRedirect(String location) throws IOException {
        if (location.startsWith("/")) {
            location = RequestURIDecodingUtils.reencode(location, urlEncoding_);
        }
        super.sendRedirect(location);
    }
}
