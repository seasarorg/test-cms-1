package org.seasar.cms.ymir.extension.creator;

import java.util.HashMap;
import java.util.Map;

public class PropertyTypeHintBag {

    private Map<String, Map<String, PropertyTypeHint>> map_ = new HashMap<String, Map<String, PropertyTypeHint>>();

    public PropertyTypeHintBag(PropertyTypeHint[] hints) {
        if (hints != null) {
            for (int i = 0; i < hints.length; i++) {
                Map<String, PropertyTypeHint> map = map_.get(hints[i]
                        .getClassName());
                if (map == null) {
                    map = new HashMap<String, PropertyTypeHint>();
                    map_.put(hints[i].getClassName(), map);
                }
                map.put(hints[i].getPropertyName(), hints[i]);
            }
        }
    }

    public PropertyTypeHint getHint(String className, String propertyName) {
        Map<String, PropertyTypeHint> map = map_.get(className);
        if (map == null) {
            return null;
        }
        return map.get(propertyName);
    }
}
