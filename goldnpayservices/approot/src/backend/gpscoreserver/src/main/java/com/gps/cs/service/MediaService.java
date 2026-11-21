package com.gps.cs.service;

import com.gps.cs.dto.AdminDtos.MediaImageResponse;
import com.gps.cs.entity.MediaImage;
import com.gps.cs.entity.MediaKind;
import com.gps.cs.exception.ApiException;
import com.gps.cs.repository.MediaImageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Homepage and event images. Files live on the images volumes and are served only through /public/images/{id}. */
@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaImageRepository repo;
    private final FileStorageService storage;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<MediaImageResponse> list(MediaKind kind, boolean activeOnly) {
        List<MediaImage> rows = activeOnly ? repo.findByKindAndActiveTrueOrderBySortOrderAscIdAsc(kind)
                : repo.findByKindOrderBySortOrderAscIdAsc(kind);
        return rows.stream().map(MediaService::toResponse).toList();
    }

    @Transactional
    public MediaImageResponse add(MediaKind kind, MultipartFile file, String title, int sortOrder) {
        FileStorageService.Stored s = storage.save(file, storage.imagesDir(kind == MediaKind.HOMEPAGE), true);
        MediaImage m = new MediaImage();
        m.setKind(kind);
        m.setFilePath(s.relativePath());
        m.setContentType(s.contentType());
        m.setTitle(title == null ? null : title.length() > 200 ? title.substring(0, 200) : title);
        m.setSortOrder(sortOrder);
        m.setActive(true);
        repo.save(m);
        audit.log("MEDIA_ADDED", "MediaImage", m.getId(), kind.name());
        return toResponse(m);
    }

    @Transactional
    public void delete(MediaKind kind, Long id) {
        MediaImage m = repo.findById(id).filter(x -> x.getKind() == kind)
                .orElseThrow(() -> ApiException.notFound("Image"));
        repo.delete(m);
        storage.deleteQuietly(m.getFilePath());
        audit.log("MEDIA_DELETED", "MediaImage", id, kind.name());
    }

    @Transactional(readOnly = true)
    public MediaImage activeImage(Long id) {
        return repo.findById(id).filter(MediaImage::isActive).orElseThrow(() -> ApiException.notFound("Image"));
    }

    public Resource content(MediaImage m) {
        return storage.load(m.getFilePath());
    }

    private static MediaImageResponse toResponse(MediaImage m) {
        return new MediaImageResponse(m.getId(), m.getKind(), m.getTitle(), m.getSortOrder(), m.isActive(),
                "/tc/v1/public/images/" + m.getId());
    }
}
