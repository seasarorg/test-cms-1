package org.seasar.cms.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * <p><b>同期化：</b>
 * このインタフェースの実装クラスはスレッドセーフである必要はありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public interface FormFile
{
    byte[] get();

    String getContentType();

    InputStream getInputStream()
        throws IOException;

    String getName();

    long getSize();

    String getString();

    String getString(String encoding)
        throws UnsupportedEncodingException;
}
