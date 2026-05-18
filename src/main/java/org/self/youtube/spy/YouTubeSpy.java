package org.self.youtube.spy;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
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

    public YouTubeSpy(String[] arguments) {
        try {
            ArgumentsProcessor argumentsProcessor = new ApacheCommonsArgumentsProcessor();
            config = argumentsProcessor.getConfig(arguments);
        } catch (Exception e) {
            System.exit(1);
        }
        try {
            youTubeService = new YouTubeService("YouTube Spy", config.getConfiguration(Config.Configuration.KEY));
        } catch (Exception e) {
            System.exit(2);
        }
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

    public void execute() throws Exception {
        if (config.hasConfiguration(Config.Configuration.TERMINAL)) {
            printVideos();
        } else {
            createWebsite();
        }
    }

    public void printVideos() throws Exception {
        List<Video> videos = youTubeService.doVideoSearch(getChannelIds(), getMaxVideos());
        /*List<Video> videos = new ArrayList<>();
        Video video1 = Video.VideoBuilder.aVideo()
                .withChannelId("7DoQPpKNN-c")
                .withId("1")
                .withThumbnailUrl("https://picsum.photos/240/135?random=1")
                .withTitle("Introduction to Web Development")
                .withChannelTitle("Code Academy")
                .withPublishedAt(Instant.now())
                .withPublishedTime(Instant.now())
                .build();
        videos.add(video1);*/
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

        // Get template and create context
        Template template = velocityEngine.getTemplate(VELOCITY_TEMPLATE);
        VelocityContext context = new VelocityContext();

        List<Video> videos = youTubeService.doVideoSearch(getChannelIds(), getMaxVideos());
        /*List<Video> videos = new ArrayList<>();
        Video video = Video.VideoBuilder.aVideo()
                .withChannelId("1")
                .withId("7DoQPpKNN-c")
                .withThumbnailUrl("https://picsum.photos/240/135?random=1")
                .withTitle("Introduction to Web Development")
                .withChannelTitle("Code Academy")
                .withPublishedAt(Instant.now())
                .build();
        videos.add(video);*/
        context.put("videos", videos);

        // Merge and output
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        try {
            Files.writeString(Paths.get(getWebsiteOutputPath()), writer.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        openWebsite();
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
        /*YouTubeSpy youTubeSpy = new YouTubeSpy(
                new String[]{
                        "-k","AIzaSyB_qRAOwp5zW7XMBjuDYehA_ZGSR_DmP0A",
                        //"-c", "UCZHhLyDll3hYHC0pyjbWFJA",
                        //"-t",
                        "-w",
                        "-f", "/tmp/channelids"
                });*/
        YouTubeSpy youTubeSpy = new YouTubeSpy(args);
        youTubeSpy.execute();
        //youTubeSpy.getSubs();
    }
}
