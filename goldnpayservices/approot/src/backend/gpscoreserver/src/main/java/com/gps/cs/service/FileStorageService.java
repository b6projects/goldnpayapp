package com.gps.cs.service;

import com.gps.cs.config.AppProperties;
import com.gps.cs.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploads under the configured volume with random names, never a public path.
 * File type is decided from the leading bytes, not from the client-supplied name or content type.
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXT = Map.of(
            "application/pdf", "pdf", "image/jpeg", "jpg", "image/png", "png");

    private final AppProperties props;

    public record Stored(String relativePath, String contentType, long size) {}

    public Path kycDir(Long userId) {
        return root(props.storage().kycFolder()).resolve(String.valueOf(userId));
    }

    public Path imagesDir(boolean homepage) {
        return root(homepage ? props.storage().hmImagesVolume() : props.storage().imagesVolume());
    }

    /** allowImagesOnly: only JPG/PNG (media); otherwise PDF/JPG/PNG (KYC). */
    public Stored save(MultipartFile file, Path dir, boolean imagesOnly) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("FILE_EMPTY", "File is empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "File exceeds 5 MB");
        }
        try {
            byte[] head = new byte[8];
            int n;
            try (InputStream in = file.getInputStream()) {
                n = in.readNBytes(head, 0, head.length);
            }
            String type = sniff(head, n);
            if (type == null || (imagesOnly && type.equals("application/pdf"))) {
                throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "FILE_TYPE_NOT_ALLOWED",
                        imagesOnly ? "Only JPG and PNG are allowed" : "Only PDF, JPG and PNG are allowed");
            }
            Files.createDirectories(dir);
            Path target = dir.resolve(UUID.randomUUID() + "." + EXT.get(type)).normalize();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new Stored(target.toString(), type, file.getSize());
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_STORE_FAILED", "Could not store the file");
        }
    }

    public Resource load(String path) {
        Path p = Path.of(path).normalize();
        if (!Files.isRegularFile(p) || !p.startsWith(root(""))) {
            throw ApiException.notFound("File");
        }
        return new FileSystemResource(p);
    }

    public void deleteQuietly(String path) {
        try {
            Path p = Path.of(path).normalize();
            if (p.startsWith(root(""))) Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // best effort
        }
    }

    private Path root(String sub) {
        return Path.of(props.storage().volumeHome(), sub == null ? "" : sub).toAbsolutePath().normalize();
    }

    private static String sniff(byte[] b, int n) {
        if (n >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') return "application/pdf";
        if (n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
        if (n >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png";
        return null;
    }
}
