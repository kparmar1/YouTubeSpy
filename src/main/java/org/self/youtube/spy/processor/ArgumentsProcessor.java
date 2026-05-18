package org.self.youtube.spy.processor;

import org.self.youtube.spy.exception.ConfigurationException;
import org.self.youtube.spy.model.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ArgumentsProcessor {

    enum ARGUMENT {
        KEY (
                "k",
                "api-key",
                true,
                true,
                "The Google API key to access google-api-services-youtube API",
                "",
                Config.Configuration.KEY.name()),
        CHANNEL_ID (
                "c",
                "channel-id",
                false,
                true,
                "The ID from the channel to search for videos",
                "A",
                Config.Configuration.CHANNEL.name()),
        FILE (
                "f",
                "file",
                false,
                true,
                "The path to the channel ID file to search with (one channel ID on each line)",
                "A",
                Config.Configuration.FILE.name()),
        HELP (
                "h",
                "help",
                false,
                false,
                "Show help message",
                "",
                ""),
        WEBSITE (
                "w",
                "website",
                false,
                false,
                "Create a website to display",
                "B",
                "website"),
        TERMINAL (
                "t",
                "terminal",
                false,
                false,
                "Output to the terminal",
                "B",
                "terminal"),
        WEBSITE_LOCATION (
                "wl",
                "website-location",
                false,
                true,
                "The output location for the website (default: /tmp/index.html)",
                "",
                Config.Configuration.WEBSITE_LOCATION.name()),
        MAX_VIDEOS (
                "m",
                "max-videos",
                false,
                true,
                "Maximum number of videos to fetch per channel (default: 5)",
                "",
                Config.Configuration.MAX_VIDEOS.name()),
        REFRESH (
                "r",
                "refresh",
                false,
                false,
                "Force refresh (ignore cache)",
                "",
                Config.Configuration.REFRESH.name()),
        CACHE_LOCATION (
                "cl",
                "cache-location",
                false,
                true,
                "Custom cache file path",
                "",
                Config.Configuration.CACHE_LOCATION.name()),
        CACHE_TTL (
                "ct",
                "cache-ttl",
                false,
                true,
                "Cache lifetime in minutes (default: 30)",
                "",
                Config.Configuration.CACHE_TTL.name()),
        CLEAR_CACHE (
                "cc",
                "clear-cache",
                false,
                false,
                "Delete cache and exit",
                "",
                Config.Configuration.CLEAR_CACHE.name());

        private final String shortName;
        private final String longName;
        private final boolean required;
        private final boolean hasValue;
        private final String description;
        private final String group;
        private final String internalKey;

        ARGUMENT(String shortName, String longName, boolean required, boolean hasValue, String description, String group, String internalKey) {
            this.shortName = shortName;
            this.longName = longName;
            this.required = required;
            this.hasValue = hasValue;
            this.description = description;
            this.group = group;
            this.internalKey = internalKey;
        }

        public String getShortName() {
            return shortName;
        }

        public String getLongName() {
            return longName;
        }

        public boolean isRequired() {
            return required;
        }

        public String getDescription() {
            return description;
        }

        public boolean isHasValue() {
            return hasValue;
        }

        public String getInternalKey() {
            return internalKey;
        }

        public static Map<String, List<ApacheCommonsArgumentsProcessor.ARGUMENT>> getGroups() {
            Map<String,List<ApacheCommonsArgumentsProcessor.ARGUMENT>> groupMap = new HashMap<>();
            for (ApacheCommonsArgumentsProcessor.ARGUMENT argument : ApacheCommonsArgumentsProcessor.ARGUMENT.values()) {
                if (!argument.group.isBlank()) {
                    String group = argument.group;
                    groupMap.computeIfAbsent(group, k -> new ArrayList<>()).add(argument);
                }
            }
            return groupMap;
        }

        public static List<ApacheCommonsArgumentsProcessor.ARGUMENT> getNoneGroups() {
            List<ApacheCommonsArgumentsProcessor.ARGUMENT> groups = new ArrayList<>();
            for (ApacheCommonsArgumentsProcessor.ARGUMENT argument : ApacheCommonsArgumentsProcessor.ARGUMENT.values()) {
                if (argument.group.isBlank()) {
                    groups.add(argument);
                }
            }
            return groups;
        }
    }

    boolean hasArgument(ApacheCommonsArgumentsProcessor.ARGUMENT argument);
    String getArgumentValue(ApacheCommonsArgumentsProcessor.ARGUMENT argument);
    Config getConfig(String[] arguments) throws ConfigurationException;
}
