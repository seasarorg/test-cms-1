/**
 * 
 */
package org.seasar.cms.framework.response.scheme;

import org.seasar.cms.framework.Response;

public interface Strategy {

    String getScheme();

    Response constructResponse(String path, Object component);
}