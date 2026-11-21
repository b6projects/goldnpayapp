package com.gps.os.service;

/** A social channel that publishes synchronously (Discord, Twitter/X). */
public interface ChannelPublisher {
    /** Channel name as used in requests: DISCORD or TWITTER. */
    String channel();

    /** Publishes text with an optional PNG/JPG image. Throws when the channel rejects the post. */
    void publish(String text, byte[] image) throws Exception;
}
