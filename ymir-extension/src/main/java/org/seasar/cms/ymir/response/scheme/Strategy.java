/**
 * 
 */
package org.seasar.cms.ymir.response.scheme;

import org.seasar.cms.ymir.Response;

public interface Strategy {

    String getScheme();

    Response constructResponse(String path, Object component);
}