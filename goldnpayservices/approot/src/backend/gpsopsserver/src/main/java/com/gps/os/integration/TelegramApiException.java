package com.gps.os.integration;

/**
 * A failed Telegram Bot API call. The message never contains the request URL, because the bot token is
 * part of it.
 */
public class TelegramApiException extends RuntimeException {
    private final int status;
    private final int retryAfterSeconds;

    public TelegramApiException(int status, int retryAfterSeconds, String message) {
        super(message);
        this.status = status;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /** HTTP status; 0 when the server could not be reached. */
    public int getStatus() { return status; }

    public int getRetryAfterSeconds() { return retryAfterSeconds; }

    /** Worth trying again: rate limited, server error or network failure. */
    public boolean isRetryable() {
        return status == 429 || status >= 500 || status == 0;
    }
}
