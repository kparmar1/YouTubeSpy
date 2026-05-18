package org.self.youtube.spy.model;

import java.util.Map;

public class Config {

    public enum Configuration {
        KEY ("key"),
        CHANNEL ("channel"),
        FILE ("file"),
        WEBSITE ("website"),
        TERMINAL ("terminal"),
        WEBSITE_LOCATION ("website-location"),
        MAX_VIDEOS ("max-videos");

        final String name;
        Configuration(String name) {
            this.name = name;
        }

        public static Configuration from(Configuration configuration) {
            return fromName(configuration.name);
        }

        public static Configuration fromName(String name) {
            for (Configuration e : Configuration.values()) {
                if (e.name().equalsIgnoreCase(name)) {
                    return e;
                }
            }
            return null;
        }
    }

    private final Map<Configuration, String> configurations;
    public Config(Map<Configuration, String> configurations) {
        this.configurations = configurations;
    }

    public Configuration[] getAllConfigKeys() {
        return Configuration.values();
    }

    public String getConfiguration(Configuration configuration) {
        return configurations.get(configuration);
    }

    public boolean hasConfigurationValue(Configuration configuration) {
        return (hasConfiguration(configuration) && (getConfiguration(configuration) != null));
    }

    public boolean hasConfiguration(Configuration configuration) {
        return configurations.containsKey(configuration);
    }
}
