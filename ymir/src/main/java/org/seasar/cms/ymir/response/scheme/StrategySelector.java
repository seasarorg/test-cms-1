/**
 * 
 */
package org.seasar.cms.framework.response.scheme;


public interface StrategySelector {

    Strategy getStrategy(String scheme);
}