package org.seasar.cms.ymir.beanutils;

import org.apache.commons.beanutils.Converter;
import org.seasar.cms.ymir.FormFile;

public class FormFileArrayConverter implements Converter {

    public Object convert(Class type, Object value) {

        if (value instanceof FormFile[]) {
            return value;
        } else if (value instanceof FormFile) {
            return new FormFile[] { (FormFile) value };
        } else {
            return null;
        }
    }
}
