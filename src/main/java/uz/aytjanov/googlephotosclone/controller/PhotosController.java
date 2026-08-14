package uz.aytjanov.googlephotosclone.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.googlephotosclone.dto.ResponseDto;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.service.PhotosService;
import uz.aytjanov.googlephotosclone.service.SuperbaseStorageService;
import java.io.IOException;
import java.util.Map;

@RestController
public class PhotosController {
    private final PhotosService photosService;
    private final SuperbaseStorageService superbaseStorageService;

    public PhotosController(PhotosService photosService, SuperbaseStorageService superbaseStorageService) {
        this.photosService = photosService;
        this.superbaseStorageService = superbaseStorageService;
    }

    private boolean checkAuthSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return !(userId == null);
    }
    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
    private boolean isPhotoBelongsToUser(Photo photo, Long userId) {
        if (photo == null) return false;
        return photo.getUser().getId().equals(userId);
    }
   @GetMapping("/api/photos")
   public ResponseEntity<Page<Photo>> photos(HttpSession session,
                                             @RequestParam(defaultValue = "")
                                             String search, @RequestParam(defaultValue = "0")
                                             int page, @RequestParam(defaultValue = "12")
                                             int size) {
       if (checkAuthSession(session)) {
           Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
           return ResponseEntity.ok(photosService.searchPhoto(search, pageable));
       }
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
   }
   @GetMapping("/api/photos/{id}")
   public ResponseEntity<byte[]> openFile(@PathVariable Long id, HttpSession session) throws IOException {
        if (checkAuthSession(session)) {
            Long userId = getUserId(session);
            Photo photo = photosService.getPhoto(id);
            if (!isPhotoBelongsToUser(photo, userId)) return ResponseEntity.notFound().build();
            return photosService.openTheFile(photo);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
   }
   @PostMapping("/api/logout")
   public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Session invalidated"));
   }

    @PostMapping("/api/photos")
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty!");
        }
        if (checkAuthSession(session)) {
            Long userId = getUserId(session);
            Photo photo = superbaseStorageService.uploadFile(userId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto(photo.getFileName(), photo.getContentType()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping("/api/photos/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable Long id, HttpSession session) {
        if (checkAuthSession(session)) {
            Long userId = getUserId(session);
            Photo photo = photosService.getPhoto(id);
            if (!photo.getUser().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            photosService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    @GetMapping("/api/photos/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, HttpSession session) throws IOException {
        if (checkAuthSession(session)) {
            Long userId = getUserId(session);
            return photosService.download(id, userId);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}