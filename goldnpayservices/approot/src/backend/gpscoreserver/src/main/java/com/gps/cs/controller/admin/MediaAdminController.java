package com.gps.cs.controller.admin;

import com.gps.cs.dto.AdminDtos.MediaImageResponse;
import com.gps.cs.entity.MediaKind;
import com.gps.cs.exception.ApiException;
import com.gps.cs.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Serves both /homepage-images (HOMEPAGE) and /event-images (EVENT). */
@Tag(name = "Admin - images")
@RestController
@RequestMapping("/tc/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','MARKETING')")
@RequiredArgsConstructor
public class MediaAdminController {
    private final MediaService media;

    @Operation(summary = "List images (kind = homepage-images | event-images)")
    @GetMapping("/{kind:homepage-images|event-images}")
    public List<MediaImageResponse> list(@PathVariable String kind) {
        return media.list(kindOf(kind), false);
    }

    @Operation(summary = "Upload a JPG/PNG image (max 5 MB)")
    @PostMapping(value = "/{kind:homepage-images|event-images}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaImageResponse add(@PathVariable String kind, @RequestPart("file") MultipartFile file,
                                  @RequestParam(required = false) String title,
                                  @RequestParam(defaultValue = "0") int sortOrder) {
        return media.add(kindOf(kind), file, title, sortOrder);
    }

    @Operation(summary = "Delete an image")
    @DeleteMapping("/{kind:homepage-images|event-images}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String kind, @PathVariable Long id) {
        media.delete(kindOf(kind), id);
        return ResponseEntity.noContent().build();
    }

    private static MediaKind kindOf(String kind) {
        return switch (kind) {
            case "homepage-images" -> MediaKind.HOMEPAGE;
            case "event-images" -> MediaKind.EVENT;
            default -> throw ApiException.notFound("Resource");
        };
    }
}
