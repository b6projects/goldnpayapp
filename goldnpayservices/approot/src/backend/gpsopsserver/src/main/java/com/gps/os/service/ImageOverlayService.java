package com.gps.os.service;

import com.gps.os.dto.MessagingDtos.LiveRate;
import com.gps.os.exception.ApiException;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

/**
 * Draws the gold rate, the date and GoldNPay branding onto a base image and returns a PNG.
 * Without a base image it draws on a gold gradient. Needs fonts at runtime: the Docker image installs
 * fontconfig and ttf-dejavu.
 */
@Service
public class ImageOverlayService {
    static final int WIDTH = 1200;
    static final int HEIGHT = 630;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    public byte[] render(byte[] baseImage, LiveRate rate, ZonedDateTime when) {
        BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            drawBackground(g, baseImage);

            // translucent panel so the text stays readable on any photo
            g.setColor(new Color(20, 14, 0, 170));
            g.fill(new RoundRectangle2D.Double(60, 60, WIDTH - 120, HEIGHT - 120, 36, 36));

            Color gold = new Color(0xFF, 0xD9, 0x6A);
            g.setColor(gold);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 54));
            g.drawString("GoldNPay", 110, 150);

            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
            g.drawString("Gold rate per gram  |  " + DATE.format(when.withZoneSameInstant(IST)) + " IST", 110, 205);

            drawRow(g, "24K", rate.rate24k(), rate.currency(), 310, gold);
            drawRow(g, "22K", rate.rate22k(), rate.currency(), 410, Color.WHITE);
            drawRow(g, "18K", rate.rate18k(), rate.currency(), 510, Color.WHITE);
        } finally {
            g.dispose();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(canvas, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not encode the image", e);
        }
    }

    private void drawRow(Graphics2D g, String label, java.math.BigDecimal value, String currency, int baseline, Color color) {
        g.setColor(color);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 64));
        g.drawString(label, 110, baseline);
        String price = String.format(Locale.ENGLISH, "%s %,.2f", "INR".equals(currency) ? "₹" : currency, value);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(price, WIDTH - 110 - fm.stringWidth(price), baseline);
    }

    private void drawBackground(Graphics2D g, byte[] baseImage) {
        if (baseImage != null && baseImage.length > 0) {
            try {
                BufferedImage base = ImageIO.read(new ByteArrayInputStream(baseImage));
                if (base == null) throw ApiException.badRequest("IMAGE_INVALID", "The image could not be read");
                double scale = Math.max((double) WIDTH / base.getWidth(), (double) HEIGHT / base.getHeight());
                int w = (int) Math.ceil(base.getWidth() * scale);
                int h = (int) Math.ceil(base.getHeight() * scale);
                g.drawImage(base, (WIDTH - w) / 2, (HEIGHT - h) / 2, w, h, null);
                return;
            } catch (IOException e) {
                throw ApiException.badRequest("IMAGE_INVALID", "The image could not be read");
            }
        }
        g.setPaint(new GradientPaint(0, 0, new Color(0x7A, 0x5C, 0x00), WIDTH, HEIGHT, new Color(0xE0, 0xB4, 0x28)));
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }
}
