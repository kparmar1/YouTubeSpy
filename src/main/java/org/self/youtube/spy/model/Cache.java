package org.self.youtube.spy.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Cache {
    private static final String DEFAULT_CACHE_DIR = System.getProperty("user.home") + "/.youtubespy/";
    private static final String DEFAULT_CACHE_FILE = "cache.json";
    private static final long DEFAULT_TTL_MINUTES = 30;

    private Map<String, ChannelCache> channels;
    private String cacheLocation;
    private long ttlMinutes;

    public Cache() {
        this.channels = new HashMap<>();
        this.cacheLocation = DEFAULT_CACHE_DIR + DEFAULT_CACHE_FILE;
        this.ttlMinutes = DEFAULT_TTL_MINUTES;
    }

    public Cache(String cacheLocation, long ttlMinutes) {
        this.channels = new HashMap<>();
        this.cacheLocation = cacheLocation != null ? cacheLocation : DEFAULT_CACHE_DIR + DEFAULT_CACHE_FILE;
        this.ttlMinutes = ttlMinutes > 0 ? ttlMinutes : DEFAULT_TTL_MINUTES;
    }

    public void load() throws IOException {
        Path path = Paths.get(cacheLocation);
        if (Files.exists(path)) {
            String content = Files.readString(path);
            CacheData data = parseCache(content);
            if (data != null) {
                this.channels = data.channels;
            }
        }
    }

    public void save() throws IOException {
        Path path = Paths.get(cacheLocation);
        Files.createDirectories(path.getParent());
        CacheData data = new CacheData(this.channels);
        String json = toJson(data);
        Files.writeString(path, json);
    }

    public boolean hasCachedData(String channelId) {
        return channels.containsKey(channelId);
    }

    public boolean isCacheFresh(String channelId) {
        if (!hasCachedData(channelId)) {
            return false;
        }
        ChannelCache channelCache = channels.get(channelId);
        Instant fetchedAt = Instant.parse(channelCache.fetchedAt);
        Instant expiration = fetchedAt.plus(ttlMinutes, ChronoUnit.MINUTES);
        return Instant.now().isBefore(expiration);
    }

    public void updateChannel(String channelId, java.util.List<Video> videos) {
        ChannelCache channelCache = new ChannelCache();
        channelCache.videos = videos;
        channelCache.fetchedAt = Instant.now().toString();
        channels.put(channelId, channelCache);
    }

    public java.util.List<Video> getCachedVideos(String channelId) {
        if (hasCachedData(channelId)) {
            return channels.get(channelId).videos;
        }
        return null;
    }

    public void clear() throws IOException {
        Path path = Paths.get(cacheLocation);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        channels.clear();
    }

    private CacheData parseCache(String json) {
        try {
            return parseCacheInternal(json);
        } catch (Exception e) {
            return null;
        }
    }

    private CacheData parseCacheInternal(String json) {
        CacheData data = new CacheData();
        data.channels = new HashMap<>();

        int channelsStart = json.indexOf("\"channels\"");
        if (channelsStart == -1) return data;

        int objStart = json.indexOf("{", channelsStart);
        int objEnd = json.lastIndexOf("}");

        String channelsJson = json.substring(objStart, objEnd + 1);
        channelsJson = channelsJson.replace("\"channels\":", "");

        int pos = 0;
        while (pos < channelsJson.length()) {
            int keyStart = channelsJson.indexOf("\"", pos);
            if (keyStart == -1) break;
            int keyEnd = channelsJson.indexOf("\"", keyStart + 1);
            if (keyEnd == -1) break;
            String channelId = channelsJson.substring(keyStart + 1, keyEnd);

            int valueStart = channelsJson.indexOf("{", keyEnd);
            int braceCount = 1;
            int valueEnd = valueStart + 1;
            while (valueEnd < channelsJson.length() && braceCount > 0) {
                if (channelsJson.charAt(valueEnd) == '{') braceCount++;
                else if (channelsJson.charAt(valueEnd) == '}') braceCount--;
                valueEnd++;
            }
            String channelJson = channelsJson.substring(valueStart, valueEnd);

            ChannelCache cc = parseChannelCache(channelJson);
            if (cc != null) {
                data.channels.put(channelId, cc);
            }

            pos = valueEnd;
        }
        return data;
    }

    private ChannelCache parseChannelCache(String json) {
        ChannelCache cc = new ChannelCache();
        cc.videos = new java.util.ArrayList<>();

        int fetchedAtStart = json.indexOf("\"fetchedAt\"");
        if (fetchedAtStart != -1) {
            int colonPos = json.indexOf(":", fetchedAtStart);
            int quoteStart = json.indexOf("\"", colonPos);
            int quoteEnd = json.indexOf("\"", quoteStart + 1);
            cc.fetchedAt = json.substring(quoteStart + 1, quoteEnd);
        }

        int videosStart = json.indexOf("\"videos\"");
        if (videosStart != -1) {
            int bracketStart = json.indexOf("[", videosStart);
            int bracketEnd = json.indexOf("]", bracketStart);
            if (bracketStart != -1 && bracketEnd != -1) {
                String videosJson = json.substring(bracketStart + 1, bracketEnd);
                cc.videos = parseVideos(videosJson);
            }
        }

        return cc;
    }

    private java.util.List<Video> parseVideos(String json) {
        java.util.List<Video> videos = new java.util.ArrayList<>();
        int pos = 0;
        while (pos < json.length()) {
            int objStart = json.indexOf("{", pos);
            if (objStart == -1) break;
            int braceCount = 1;
            int objEnd = objStart + 1;
            while (objEnd < json.length() && braceCount > 0) {
                if (json.charAt(objEnd) == '{') braceCount++;
                else if (json.charAt(objEnd) == '}') braceCount--;
                objEnd++;
            }
            String videoJson = json.substring(objStart, objEnd);
            Video video = parseVideo(videoJson);
            if (video != null) {
                videos.add(video);
            }
            pos = objEnd;
        }
        return videos;
    }

    private Video parseVideo(String json) {
        String channelId = extractString(json, "channelId");
        String channelTitle = extractString(json, "channelTitle");
        String id = extractString(json, "id");
        String title = extractString(json, "title");
        String description = extractString(json, "description");
        String thumbnailUrl = extractString(json, "thumbnailUrl");
        Long thumbnailWidth = extractLong(json, "thumbnailWidth");
        Long thumbnailHeight = extractLong(json, "thumbnailHeight");
        Instant publishedAt = extractInstant(json, "publishedAt");
        Instant publishedTime = extractInstant(json, "publishedTime");

        return new Video.VideoBuilder()
                .withChannelId(channelId)
                .withChannelTitle(channelTitle)
                .withId(id)
                .withTitle(title)
                .withDescription(description)
                .withThumbnailUrl(thumbnailUrl)
                .withThumbnailWidth(thumbnailWidth)
                .withThumbnailHeight(thumbnailHeight)
                .withPublishedAt(publishedAt)
                .withPublishedTime(publishedTime)
                .build();
    }

    private String extractString(String json, String key) {
        int keyPos = json.indexOf("\"" + key + "\"");
        if (keyPos == -1) return null;
        int colonPos = json.indexOf(":", keyPos);
        int quoteStart = json.indexOf("\"", colonPos);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private Long extractLong(String json, String key) {
        int keyPos = json.indexOf("\"" + key + "\"");
        if (keyPos == -1) return null;
        int colonPos = json.indexOf(":", keyPos);
        int valueEnd = json.indexOf(",", colonPos);
        if (valueEnd == -1) valueEnd = json.indexOf("}", colonPos);
        String value = json.substring(colonPos + 1, valueEnd).trim();
        if (value.equals("null")) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Instant extractInstant(String json, String key) {
        String value = extractString(json, key);
        if (value == null) return null;
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(CacheData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"channels\": {\n");

        var entries = data.channels.entrySet();
        int i = 0;
        for (var entry : entries) {
            if (i > 0) sb.append(",\n");
            sb.append("    \"").append(entry.getKey()).append("\": {\n");
            sb.append("      \"fetchedAt\": \"").append(entry.getValue().fetchedAt).append("\",\n");
            sb.append("      \"videos\": [");

            var videos = entry.getValue().videos;
            for (int j = 0; j < videos.size(); j++) {
                if (j > 0) sb.append(", ");
                Video v = videos.get(j);
                sb.append("{");
                sb.append("\"channelId\":\"").append(nullSafe(v.channelId())).append("\",");
                sb.append("\"channelTitle\":\"").append(nullSafe(v.channelTitle())).append("\",");
                sb.append("\"id\":\"").append(nullSafe(v.id())).append("\",");
                sb.append("\"title\":\"").append(nullSafe(v.title())).append("\",");
                sb.append("\"description\":\"").append(nullSafe(v.description())).append("\",");
                sb.append("\"thumbnailUrl\":\"").append(nullSafe(v.thumbnailUrl())).append("\",");
                sb.append("\"thumbnailWidth\":").append(v.thumbnailWidth()).append(",");
                sb.append("\"thumbnailHeight\":").append(v.thumbnailHeight()).append(",");
                sb.append("\"publishedAt\":\"").append(v.publishedAt()).append("\",");
                sb.append("\"publishedTime\":\"").append(v.publishedTime()).append("\"");
                sb.append("}");
            }

            sb.append("]\n    }");
            i++;
        }

        sb.append("\n  }\n}");
        return sb.toString();
    }

    private String nullSafe(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    private static class CacheData {
        Map<String, ChannelCache> channels;

        CacheData() {}

        CacheData(Map<String, ChannelCache> channels) {
            this.channels = channels;
        }
    }

    private static class ChannelCache {
        java.util.List<Video> videos;
        String fetchedAt;
    }
}