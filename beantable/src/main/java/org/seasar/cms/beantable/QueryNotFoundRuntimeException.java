package org.seasar.cms.beantable;


/**
 * @author YOKOTA Takehiko
 */
public class QueryNotFoundRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 2347775084968501604L;


    public QueryNotFoundRuntimeException()
    {
        super();
    }


    /**
     * @param message
     */
    public QueryNotFoundRuntimeException(String message)
    {
        super(message);
    }


    /**
     * @param cause
     */
    public QueryNotFoundRuntimeException(Throwable cause)
    {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public QueryNotFoundRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
