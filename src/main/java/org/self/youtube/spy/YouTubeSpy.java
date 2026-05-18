package org.self.youtube.spy;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.self.youtube.spy.model.Cache;
import org.self.youtube.spy.model.Config;
import org.self.youtube.spy.model.Video;
import org.self.youtube.spy.service.ApacheCommonsArgumentsProcessor;
import org.self.youtube.spy.processor.ArgumentsProcessor;
import org.self.youtube.spy.service.YouTubeService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class YouTubeSpy {

    private static final String VELOCITY_TEMPLATE = "templates/index.vm";

    private static final String DEFAULT_WEBSITE_OUTPUT = "/tmp/index.html";
    private static Config config;

    private YouTubeService youTubeService;
    private Cache cache;

    public YouTubeSpy(String[] arguments) {
        try {
            ArgumentsProcessor argumentsProcessor = new ApacheCommonsArgumentsProcessor();
            config = argumentsProcessor.getConfig(arguments);
        } catch (Exception e) {
            System.exit(1);
        }

        if (config.hasConfiguration(Config.Configuration.CLEAR_CACHE)) {
            try {
                Cache clearCache = createCache();
                clearCache.clear();
                System.out.println("Cache cleared successfully.");
                System.exit(0);
            } catch (IOException e) {
                System.out.println("Failed to clear cache: " + e.getMessage());
                System.exit(1);
            }
        }

        try {
            youTubeService = new YouTubeService("YouTube Spy", config.getConfiguration(Config.Configuration.KEY));
            cache = createCache();
            cache.load();
        } catch (Exception e) {
            System.exit(2);
        }
    }

    private Cache createCache() {
        String cacheLocation = null;
        long ttlMinutes = 30;

        if (config.hasConfigurationValue(Config.Configuration.CACHE_LOCATION)) {
            cacheLocation = config.getConfiguration(Config.Configuration.CACHE_LOCATION);
        }
        if (config.hasConfigurationValue(Config.Configuration.CACHE_TTL)) {
            ttlMinutes = Long.parseLong(config.getConfiguration(Config.Configuration.CACHE_TTL));
        }

        return new Cache(cacheLocation, ttlMinutes);
    }

    private List<String> getChannelIds() throws Exception {
        List<String> channelIds = new ArrayList<>();
        if (config.hasConfiguration(Config.Configuration.FILE)) {
            channelIds.addAll(getChannelIdsFromFile(config.getConfiguration(Config.Configuration.FILE)));
        } else {
            channelIds.add(config.getConfiguration(Config.Configuration.CHANNEL));
        }
        return channelIds;
    }

    private List<String> getChannelIdsFromFile(String file) throws IOException {
        List<String> lines;
        try (var stream = Files.lines(Paths.get(file))) {
            lines = stream.toList();
        }
        return lines;
    }

    private String getWebsiteOutputPath() {
        if (config.hasConfiguration(Config.Configuration.WEBSITE_LOCATION)) {
            return config.getConfiguration(Config.Configuration.WEBSITE_LOCATION);
        }
        return DEFAULT_WEBSITE_OUTPUT;
    }

    private long getMaxVideos() {
        if (config.hasConfigurationValue(Config.Configuration.MAX_VIDEOS)) {
            return Long.parseLong(config.getConfiguration(Config.Configuration.MAX_VIDEOS));
        }
        return 5;
    }

    private boolean shouldRefresh() {
        return config.hasConfiguration(Config.Configuration.REFRESH);
    }

    public void execute() throws Exception {
        if (config.hasConfiguration(Config.Configuration.TERMINAL)) {
            printVideos();
        } else {
            createWebsite();
        }
    }

    public void printVideos() throws Exception {
        List<Video> videos = getVideos(getChannelIds(), getMaxVideos());
        for (Video video : videos) {
            System.out.println(video);
        }
    }

    public void createWebsite() throws Exception {
        VelocityEngine velocityEngine = new VelocityEngine();

        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init(props);

        Template template = velocityEngine.getTemplate(VELOCITY_TEMPLATE);
        VelocityContext context = new VelocityContext();

        List<Video> videos = getVideos(getChannelIds(), getMaxVideos());
        context.put("videos", videos);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        try {
            Files.writeString(Paths.get(getWebsiteOutputPath()), writer.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        openWebsite();
    }

    private List<Video> getVideos(List<String> channelIds, long maxResults) throws Exception {
        List<Video> allVideos = new ArrayList<>();

        for (String channelId : channelIds) {
            List<Video> videos = getVideosForChannel(channelId, maxResults);
            allVideos.addAll(videos);
        }

        return allVideos;
    }

    private List<Video> getVideosForChannel(String channelId, long maxResults) throws Exception {
        if (!shouldRefresh() && cache.isCacheFresh(channelId)) {
            return cache.getCachedVideos(channelId);
        }

        List<Video> videos = youTubeService.doVideoSearch(channelId, maxResults);
        cache.updateChannel(channelId, videos);
        cache.save();

        return videos;
    }

    private void openWebsite() throws Exception {
        File file = new File(getWebsiteOutputPath());
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.open(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        YouTubeSpy youTubeSpy = new YouTubeSpy(args);
        youTubeSpy.execute();
    }
}