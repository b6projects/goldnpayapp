package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.gps.os.config.AppProperties;
import com.gps.os.dto.MessagingDtos.LiveRate;
import com.gps.os.service.DiscordPublisher;
import com.gps.os.service.HtmlSanitizer;
import com.gps.os.service.ImageOverlayService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class UnitTests {

    private static final LiveRate RATE = new LiveRate("LIVE", new BigDecimal("7500.50"), new BigDecimal("6875.00"),
            new BigDecimal("5625.00"), "INR", Instant.now());

    @Test
    void overlay_returns_a_png_of_the_expected_size() throws Exception {
        byte[] png = new ImageOverlayService().render(null, RATE, ZonedDateTime.now());
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(img.getWidth()).isEqualTo(1200);
        assertThat(img.getHeight()).isEqualTo(630);
    }

    @Test
    void overlay_draws_on_a_supplied_base_image() throws Exception {
        BufferedImage base = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) for (int y = 0; y < 300; y++) base.setRGB(x, y, 0x2040FF);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(base, "png", out);
        byte[] png = new ImageOverlayService().render(out.toByteArray(), RATE, ZonedDateTime.now());
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(img.getWidth()).isEqualTo(1200); // scaled to cover the canvas
        assertThatThrownBy(() -> new ImageOverlayService().render("not an image".getBytes(), RATE, ZonedDateTime.now()))
                .hasMessageContaining("could not be read");
    }

    @Test
    void sanitizer_keeps_formatting_and_drops_active_content() {
        HtmlSanitizer s = new HtmlSanitizer();
        String out = s.clean("<p style=\"color:red\" onmouseover=\"x()\">Hi <b>there</b></p><SCRIPT>bad()</SCRIPT>"
                + "<a href=\" JaVaScRiPt:evil()\">l</a><a href=\"https://goldnpay.in\">ok</a><iframe src=\"x\"></iframe>");
        assertThat(out).contains("<b>there</b>").contains("color:red").contains("https://goldnpay.in")
                .doesNotContainIgnoringCase("script").doesNotContain("onmouseover").doesNotContainIgnoringCase("javascript:")
                .doesNotContain("iframe");
    }

    @Test
    void discord_publishes_json_without_an_image_and_multipart_with_one() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();
        DiscordPublisher d = new DiscordPublisher(b, props("http://discord.test/api/webhooks/1/secret"));

        server.expect(requestTo("http://discord.test/api/webhooks/1/secret"))
                .andExpect(jsonPath("$.content").value("hello")).andRespond(withSuccess());
        server.expect(requestTo("http://discord.test/api/webhooks/1/secret"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA)).andRespond(withSuccess());
        d.publish("hello", null);
        d.publish("with image", new byte[]{1, 2, 3});
        server.verify();
    }

    @Test
    void discord_errors_never_reveal_the_webhook_url() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();
        DiscordPublisher d = new DiscordPublisher(b, props("http://discord.test/api/webhooks/1/secret"));
        server.expect(requestTo("http://discord.test/api/webhooks/1/secret")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> d.publish("x", null)).hasMessageContaining("404").hasMessageNotContaining("secret");
    }

    private static AppProperties props(String webhook) {
        return new AppProperties("TEST", null, null, null, null, null, new AppProperties.Discord(true, webhook), null, null, null);
    }

    @Test
    void disabled_social_integrations_need_no_credentials_but_enabled_ones_do() {
        // Telegram and Twitter off: their credentials may be empty, startup must not complain about them.
        // Everything else is still missing here, so the failure must name those and none of the disabled ones.
        Throwable failure = org.assertj.core.api.Assertions.catchThrowable(
                () -> new SpringApplicationBuilder(GpsOpsServerApplication.class)
                        .web(WebApplicationType.NONE)
                        .properties("spring.profiles.active=nosuchprofile")
                        // command-line arguments outrank application.yml (builder properties would not)
                        .run("--app.telegram.enabled=false", "--app.twitter.enabled=false", "--app.discord.enabled=true"));
        assertThat(failure).hasStackTraceContaining("JWT_SECRET_KEY").hasStackTraceContaining("DISCORD_WEBHOOK_URL");
        var trace = new java.io.StringWriter();
        failure.printStackTrace(new java.io.PrintWriter(trace));
        assertThat(trace.toString()).doesNotContain("TGM_SUPPORT_BOT_TOKEN").doesNotContain("TWITTER_ACCESS_SECRET");
    }

    @Test
    void server_refuses_to_start_when_secrets_are_missing_and_names_them() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(GpsOpsServerApplication.class)
                .web(WebApplicationType.NONE).properties("spring.profiles.active=nosuchprofile")
                        .run("--app.telegram.enabled=true", "--app.discord.enabled=true", "--app.twitter.enabled=true"))
                .hasStackTraceContaining("Missing required configuration")
                .hasStackTraceContaining("JWT_SECRET_KEY")
                .hasStackTraceContaining("MAIL_PASSWORD")
                .hasStackTraceContaining("TGM_SUPPORT_BOT_TOKEN")
                .hasStackTraceContaining("TWITTER_ACCESS_SECRET");
    }
}
