package org.seasar.cms.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private Properties properties_ = new Properties();

    public Configuration() {
    }

    public void load(String configPath) {

        InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(configPath);
        if (is == null) {
            throw new IllegalArgumentException(
                "Configuration resource does not exist: path=" + configPath);
        }
        try {
            properties_.clear();
            properties_.load(is);
        } catch (IOException ex) {
            throw new RuntimeException(
                "Can't load configration resource: path=" + configPath, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

    public String getProperty(String key) {

        String value = properties_.getProperty(key);
        if (value != null) {
            return value;
        } else {
            throw new RuntimeException("Property does not exist: key=" + key);
        }
    }

    public String getProperty(String key, String defaultValue) {

        return properties_.getProperty(key, defaultValue);
    }
}
