package org.seasar.cms.beantable;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。</p>
 *
 * @author YOKOTA Takehiko
 */
public class Pair {

    private String template_;

    private Object[] params_;

    public Pair(String template, Object[] params) {
        template_ = template;
        params_ = params;
    }

    public String getTemplate() {
        return template_;
    }

    public Object[] getParameters() {
        return params_;
    }
}
