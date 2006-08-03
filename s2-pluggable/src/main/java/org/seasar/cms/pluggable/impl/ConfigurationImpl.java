package org.seasar.cms.pluggable.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.framework.log.Logger;

public class ConfigurationImpl implements Configuration {

    private Properties properties_ = new Properties();

    private Logger log_ = Logger.getLogger(getClass());

    public void load(String configPath) {

        load(new String[] { configPath });
    }

    public void load(String[] configPaths) {

        properties_.clear();
        for (int i = 0; i < configPaths.length; i++) {
            load0(configPaths[i]);
        }
    }

    void load0(String configPath) {

        InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(configPath);
        if (is != null) {
            try {
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
        } else {
            log_.info("Configuration resource does not exist: path="
                + configPath);
        }
    }

    public String getProperty(String key) {

        return properties_.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {

        return properties_.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {

        properties_.setProperty(key, value);
    }

    public void removeProperty(String key) {

        properties_.remove(key);
    }

    public void save(OutputStream out, String header) throws IOException {

        properties_.store(out, header);
    }

    public boolean equalsProjectStatus(String status) {

        return status.equals(getProperty(KEY_PROJECTSTATUS));
    }
}
