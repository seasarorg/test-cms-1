package org.seasar.cms.ymir;

public class Note {

    private String value_;

    private Object[] parameters_;

    public Note(String value) {
        this(value, null);
    }

    public Note(String value, Object parameter0) {
        this(value, new Object[] { parameter0 }, false);
    }

    public Note(String value, Object parameter0, Object parameter1) {
        this(value, new Object[] { parameter0, parameter1 }, false);
    }

    public Note(String value, Object parameter0, Object parameter1,
            Object parameter2) {
        this(value, new Object[] { parameter0, parameter1, parameter2 }, false);
    }

    public Note(String value, Object parameter0, Object parameter1,
            Object parameter2, Object parameter3) {
        this(value, new Object[] { parameter0, parameter1, parameter2,
            parameter3 }, false);
    }

    public Note(String value, Object[] parameters) {
        this(value, parameters, true);
    }

    /**
     * このクラスのオブジェクトを構築します。
     * <p>このコンストラクタはインスタンスを不変にするために使用されます。</p>
     *
     * @param value メッセージの内容。
     * @param parameters パラメータ列。nullを指定しても構いません。
     * @param clone パラメータ列を複製するかどうか。
     */
    private Note(String value, Object[] parameters, boolean clone) {
        value_ = value;
        if (parameters == null) {
            parameters_ = new Object[0];
        } else {
            if (clone) {
                Object[] objs = new Object[parameters.length];
                System.arraycopy(parameters, 0, objs, 0, parameters.length);
                parameters_ = objs;
            } else {
                parameters_ = parameters;
            }
        }
    }

    /*
     * public scope methods
     */

    public String getValue() {
        return value_;
    }

    public Object[] getParameters() {
        return parameters_;
    }
}
