package org.self.youtube.spy.model;

import java.util.Map;

public class Config {

    public enum Configuration {
        KEY ("key"),
        CHANNEL ("channel"),
        FILE ("file"),
        WEBSITE ("website"),
        TERMINAL ("terminal");

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

    public String getConfiguation(Configuration configuration) {
        return configurations.get(configuration);
    }

    public boolean hasConfiguationValue(Configuration configuration) {
        return (hasConfiguration(configuration) && (getConfiguation(configuration) != null));
    }

    public boolean hasConfiguration(Configuration configuration) {
        return configurations.containsKey(configuration);
    }
}
