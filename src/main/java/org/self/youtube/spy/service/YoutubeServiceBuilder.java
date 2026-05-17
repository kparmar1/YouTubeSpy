package org.self.youtube.spy.service;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

public class YoutubeServiceBuilder {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static YouTube getService(String applicationName) throws Exception {
        return new YouTube.Builder(new NetHttpTransport(), JSON_FACTORY, (HttpRequestInitializer) null)
                .setApplicationName(applicationName).build();
    }
}
