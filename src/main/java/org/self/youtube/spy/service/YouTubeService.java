package org.self.youtube.spy.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import org.self.youtube.spy.model.Video;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class YouTubeService {

    private static YouTube youTube;
    final String apiKey;

    public YouTubeService(String applicationName, String apiKey) throws Exception {
        this.apiKey = apiKey;
        youTube = YoutubeServiceBuilder.getService(applicationName);
    }

    public List<Video> doVideoSearch(List<String> channelIds, long maxResults) throws Exception {
        List<Video> videos = new ArrayList<>();
        for (String channelId : channelIds) {
            try {
                videos.addAll(doVideoSearch(channelId, maxResults));
            } catch (Exception e) {
                System.out.println("Error looking up channelId: " + channelId);
                e.printStackTrace();
                throw e;
            }
        }
        return videos;
    }

    public List<Video> doVideoSearch(String channelId, long maxResults) throws Exception {

        List<Video> videos = new ArrayList<>();

        YouTube.Search.List request = youTube.search().list(List.of("snippet"));
        request.setKey(apiKey);
        request.setChannelId(channelId);
        request.setOrder("date"); // Sort by publish date (newest first)
        request.setMaxResults(maxResults);

        SearchListResponse response = request.execute();
        for (SearchResult item : response.getItems()) {
            Video.VideoBuilder builder = new Video.VideoBuilder();

            builder.withId(item.getId().getVideoId())
                    .withChannelId(item.getSnippet().getChannelId())
                    .withChannelTitle(item.getSnippet().getChannelTitle())
                    .withDescription(item.getSnippet().getDescription())
                    .withTitle(item.getSnippet().getTitle())
                    .withPublishedAt(Instant.ofEpochMilli(item.getSnippet().getPublishedAt().getValue()))
                    .withPublishedTime(Instant.ofEpochMilli(item.getSnippet().getPublishedAt().getValue()));

            Thumbnail thumbnail = getThumbnail(item.getSnippet().getThumbnails());
            Video video = null;
            if (thumbnail != null) {
                video = builder.withThumbnailUrl(thumbnail.getUrl())
                        .withThumbnailHeight(thumbnail.getHeight())
                        .withThumbnailWidth(thumbnail.getWidth()).build();
            }
            if (video != null) {
                videos.add(video);
            }
        }
        return videos;
    }

    private Thumbnail getThumbnail(ThumbnailDetails thumbnailDetails) {
        if (thumbnailDetails.getMaxres() != null) return thumbnailDetails.getMaxres();
        if (thumbnailDetails.getHigh() != null) return thumbnailDetails.getHigh();
        if (thumbnailDetails.getMedium() != null) return thumbnailDetails.getMedium();
        if (thumbnailDetails.getStandard() != null) return thumbnailDetails.getStandard();
        if (thumbnailDetails.getDefault() != null) return thumbnailDetails.getDefault();
        return null;
    }
}
