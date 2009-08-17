package org.seasar.cms.mailsender.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * メールの件名を指定するためのアノテーションです。
 * 
 * @author skirnir
 * @since 0.0.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subject {
    /**
     * 件名です。
     * <p>{@link #value()}か{@link #template()}か
     * どちらか一方は指定する必要があります。
     * </p>
     * 
     * @return 件名
     */
    String value() default "";

    /**
     * 件名を構築するためのテンプレート名です。
     * <p>{@link #value()}か{@link #template()}か
     * どちらか一方は指定する必要があります。
     * </p>
     * 
     * @return 件名を構築するためのテンプレート名
     */
    String template() default "";
}
