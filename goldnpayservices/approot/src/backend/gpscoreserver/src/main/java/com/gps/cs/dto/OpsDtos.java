package com.gps.cs.dto;

import java.util.List;

/** Request contracts for Core -> Ops calls (spec section 7). */
public final class OpsDtos {
    private OpsDtos() {}

    public record Attachment(String fileName, String contentType, String base64) {}

    /** content is body HTML only; Ops owns the template (D1). */
    public record EmailRequest(String requestId, String templateCode, List<String> to, List<String> cc,
                               List<String> bcc, String subject, String content, List<Attachment> attachments,
                               String category, Long userId) {}

    /** overlayRate: ask Ops to draw the live gold rate onto the image (used when a new rate goes live). */
    public record SocialPostRequest(String requestId, String text, String imageBase64, List<String> channels,
                                    boolean overlayRate) {}

    public record TgmAppMessage(String requestId, Long userId, String text) {}

    public record TgmPrMessage(String requestId, String text, String imageBase64) {}
}
