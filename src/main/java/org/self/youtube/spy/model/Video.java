package org.self.youtube.spy.model;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record Video(String channelId, String channelTitle, String id, String title, String description,
                    String thumbnailUrl, Long thumbnailWidth, Long thumbnailHeight, Instant publishedAt,
                    Instant publishedTime) {
    
    public Long getPublishedAtInEpochMilli() {
        return publishedAt.toEpochMilli();
    }

    public String getPublishedAtString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneOffset.UTC);
        return formatter.format(publishedAt);
    }

    public String getPublishedTimeString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneOffset.UTC);
        return formatter.format(publishedTime);
    }

    public static final class VideoBuilder {

        private String channelId;
        private String channelTitle;
        private String id;
        private String title;
        private String description;
        private String thumbnailUrl;
        private Long thumbnailWidth;
        private Long thumbnailHeight;
        private Instant publishedAt;
        private Instant publishedTime;

        public VideoBuilder() {
        }

        public VideoBuilder(Video other) {
            this.channelId = other.channelId;
            this.channelTitle = other.channelTitle;
            this.id = other.id;
            this.title = other.title;
            this.description = other.description;
            this.thumbnailUrl = other.thumbnailUrl;
            this.thumbnailWidth = other.thumbnailWidth;
            this.thumbnailHeight = other.thumbnailHeight;
            this.publishedAt = other.publishedAt;
            this.publishedTime = other.publishedTime;
        }

        public static VideoBuilder aVideo() {
            return new VideoBuilder();
        }

        public VideoBuilder withChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public VideoBuilder withChannelTitle(String channelTitle) {
            this.channelTitle = channelTitle;
            return this;
        }

        public VideoBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public VideoBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public VideoBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public VideoBuilder withThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public VideoBuilder withThumbnailWidth(Long thumbnailWidth) {
            this.thumbnailWidth = thumbnailWidth;
            return this;
        }

        public VideoBuilder withThumbnailHeight(Long thumbnailHeight) {
            this.thumbnailHeight = thumbnailHeight;
            return this;
        }


        public VideoBuilder withPublishedTime(Instant publishedTime) {
            this.publishedTime = publishedTime;
            return this;
        }

        public VideoBuilder withPublishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Video build() {
            return new Video(channelId, channelTitle, id, title, description, thumbnailUrl, thumbnailWidth, thumbnailHeight, publishedAt, publishedTime);
        }
    }
}
