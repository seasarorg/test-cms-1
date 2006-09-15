package org.seasar.cms.ymir.response.constructor;

import org.seasar.cms.ymir.Response;

public interface ResponseConstructor {

    /**
     * このResponseConstructorが処理する対象のクラスを返します。
     * 
     * @return 処理対象のクラス。
     */
    Class getTargetClass();

    /**
     * 指定されたコンポーネントとアクションメソッドの返り値からResponseを構築して返します。
     * 
     * @param component コンポーネント。nullであることがあります。
     * @param returnValue 返り値。nullであることがあります。
     * @return 構築したResponseオブジェクト。
     */
    Response constructResponse(Object component, Object returnValue);
}
