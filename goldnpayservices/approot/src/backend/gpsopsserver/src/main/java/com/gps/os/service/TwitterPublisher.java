package com.gps.os.service;

import com.gps.os.config.AppProperties;
import java.io.ByteArrayInputStream;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import org.springframework.stereotype.Service;

/**
 * Posts to Twitter/X through twitter4j 4.0.7 (the version in the spec), which uses the v1.1 API.
 * X has restricted v1.1 posting on most API tiers, so a rejected post is reported as FAILED for the
 * channel and never blocks the others. Check the credentials and tier before relying on this channel.
 */
@Service
public class TwitterPublisher implements ChannelPublisher {
    private static final int LIMIT = 280;
    private final AppProperties props;
    private volatile Twitter twitter;

    public TwitterPublisher(AppProperties props) {
        this.props = props;
    }

    @Override
    public String channel() {
        return "TWITTER";
    }

    @Override
    public void publish(String text, byte[] image) {
        String status = text.length() > LIMIT ? text.substring(0, LIMIT - 1) + "…" : text;
        try {
            StatusUpdate update = new StatusUpdate(status);
            if (image != null && image.length > 0) {
                update.setMedia("gold.png", new ByteArrayInputStream(image));
            }
            client().updateStatus(update);
        } catch (TwitterException e) {
            // never include e.getMessage(): it can echo request details
            throw new IllegalStateException("Twitter rejected the post: HTTP " + e.getStatusCode()
                    + (e.getErrorCode() > 0 ? " code " + e.getErrorCode() : ""));
        }
    }

    private Twitter client() {
        Twitter t = twitter;
        if (t == null) {
            synchronized (this) {
                if (twitter == null) {
                    var cfg = new ConfigurationBuilder()
                            .setOAuthConsumerKey(props.twitter().apiKey())
                            .setOAuthConsumerSecret(props.twitter().apiSecret())
                            .setOAuthAccessToken(props.twitter().accessToken())
                            .setOAuthAccessTokenSecret(props.twitter().accessSecret())
                            .build();
                    twitter = new TwitterFactory(cfg).getInstance();
                }
                t = twitter;
            }
        }
        return t;
    }
}
