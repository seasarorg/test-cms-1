package org.seasar.cms.ymir.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.ExceptionHandler;
import org.seasar.kvasir.util.io.IORuntimeException;

public class PageNotFoundExceptionHandler implements ExceptionHandler {

    private HttpServletResponse httpResponse_;

    public void setHttpResponse(HttpServletResponse httpResponse) {

        httpResponse_ = httpResponse;
    }

    public String handle(Throwable t) {

        try {
            httpResponse_.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }
    }
}
