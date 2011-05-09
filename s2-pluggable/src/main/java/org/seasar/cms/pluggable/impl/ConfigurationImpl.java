package org.seasar.cms.pluggable.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.pluggable.Configuration;

public class ConfigurationImpl implements Configuration {
    private Properties properties_ = new Properties();

    private Log log_ = LogFactory.getLog(ConfigurationImpl.class);

    public void load(String configPath) {
        load(new String[] { configPath });
    }

    public void load(String[] configPaths) {
        Properties properties = null;
        for (int i = configPaths.length - 1; i >= 0; i--) {
            properties = load0(configPaths[i], properties);
        }
        if (properties == null) {
            properties = new Properties();
        }

        properties_ = properties;
    }

    Properties load0(String configPath, Properties defaults) {
        InputStream is = null;
        if (configPath.indexOf(':') >= 0) {
            try {
                is = new URL(configPath).openStream();
            } catch (MalformedURLException ignored) {
            } catch (IOException ex) {
                log_.info("Can't open configuration resource: path="
                        + configPath);
                if (log_.isDebugEnabled()) {
                    log_.debug("Can't open configuration resource: path="
                            + configPath, ex);
                }
                return defaults;
            }
        }
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(configPath);
        }
        if (is != null) {
            try {
                Properties properties = new Properties(defaults);
                properties.load(is);
                return properties;
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Can't load configration resource: path=" + configPath,
                        ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
        } else {
            log_.info("Configuration resource does not exist: path="
                    + configPath);
            return defaults;
        }
    }

    public String getProperty(String key) {
        return properties_.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties_.getProperty(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public Enumeration<String> propertyNames() {
        return (Enumeration<String>) properties_.propertyNames();
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

    public boolean isUnderDevelopment() {
        return equalsProjectStatus(PROJECTSTATUS_DEVELOP);
    }
}
