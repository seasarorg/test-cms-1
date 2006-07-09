/**
 * 
 */
package org.seasar.cms.ymir.response.scheme;


public interface StrategySelector {

    Strategy getStrategy(String scheme);
}