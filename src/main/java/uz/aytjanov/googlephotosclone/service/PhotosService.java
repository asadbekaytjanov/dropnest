package uz.aytjanov.googlephotosclone.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.googlephotosclone.dto.PhotoListDto;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.repository.PhotosRepository;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;


import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PhotosService {
    private final PhotosRepository photosRepository;

    public PhotosService(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }
    // Get a specific photo
    public Photo getPhoto(Long id) {
        Optional<Photo> photo = photosRepository.findById(id);
        if (photo.isEmpty()) throw new ResponseStatusException(NOT_FOUND);
        return photo.orElse(null);
    }
    // Download a photo
    public ResponseEntity<byte[]> download(Long id, Long userId) throws IOException {
        Photo photo = getPhoto(id);
        if (!photo.getUser().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        byte[] data;
        try (InputStream in = URI.create(photo.getFilePath()).toURL().openStream()) {
            data = in.readAllBytes();
        }
        String contentType = photo.getContentType();
        MediaType mediaType;
        // MediaType handling
        try {
            if (contentType == null || contentType.isBlank()) {
                mediaType = MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            }
            else {
                mediaType = MediaType.valueOf(contentType);
            }
        } catch (Exception e) {
            mediaType = MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(photo.getFileName()).build());
        headers.setContentType(mediaType);
        // return a data with headers with HttpStatus OK
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
    public Page<Photo> searchPhoto(String search, Pageable pageable) {
        Page<Photo> result;
        if (search.isBlank()) result = photosRepository.findAll(pageable);
        else {
            result = photosRepository.findByFileNameContainingIgnoreCase(search, pageable);
        }
        return result;
    }
    // Open a specific file
    public ResponseEntity<byte[]> openTheFile(Photo photo) throws IOException {
        byte[] data;
        try (InputStream in = URI.create(photo.getFilePath()).toURL().openStream()) {
            data = in.readAllBytes();
        }
        return ResponseEntity.ok().header("Content-Type", photo.getContentType()).body(data);
    }
    // deleting a file
    @Transactional
    public void delete(Long id) {
        photosRepository.removeById(id);
    }
    public List<PhotoListDto> getAllUsersPhotosAsDtos(Long userId) {
        return photosRepository.findByUserId(userId).stream()
                .map(photo -> new PhotoListDto(photo.getId(), photo.getFileName(), photo.getContentType()))
                .toList();
    }
}