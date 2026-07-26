package uz.aytjanov.googlephotosclone.service;

import com.sun.net.httpserver.Headers;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.ServerRequest;
import uz.aytjanov.googlephotosclone.dto.PhotoListDto;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.repository.PhotosRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PhotosService {
    private final PhotosRepository photosRepository;

    public PhotosService(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }
    public Photo getPhoto(Long id) {
        Optional<Photo> photo = photosRepository.findById(id);
        if (photo.isEmpty()) throw new ResponseStatusException(NOT_FOUND);
        return photo.orElse(null);
    }

    public void savePhoto(Photo photo) {
        photosRepository.save(photo);
    }
    public List<Photo> getMediaByUserid(Long userId) {
       return photosRepository.findByUserId(userId);
    }

    public Photo createAndSave(Long userId, MultipartFile file) throws IOException {
        String UPLOAD_DIRECTORY = "uploads/";
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String uniqueFileName = UUID.randomUUID() + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        file.transferTo(filePath);
        Photo photo = new Photo();
        photo.setContentType(file.getContentType());
        photo.setFileName(file.getOriginalFilename());
        photo.setFilePath(filePath.toString());
        return photo;
    }
    public ResponseEntity<byte[]> download(Long id, Long userId) throws IOException {
        Photo photo = getPhoto(id);
        if (!photo.getUser().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Path filePath = Path.of(photo.getFilePath());
        byte[] data = Files.readAllBytes(filePath);
        String contentType = photo.getContentType();
        MediaType mediaType;
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
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
    public ResponseEntity<byte[]> openTheFile(Photo photo) throws IOException {
        byte[] data = Files.readAllBytes(Path.of(photo.getFilePath()));
        return ResponseEntity.ok().header("Content-Type", photo.getContentType()).body(data);
    }
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