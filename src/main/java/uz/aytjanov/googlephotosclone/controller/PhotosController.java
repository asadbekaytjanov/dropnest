package uz.aytjanov.googlephotosclone.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.googlephotosclone.dto.PhotoListDto;
import uz.aytjanov.googlephotosclone.dto.ResponseDto;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.service.PhotosService;
import uz.aytjanov.googlephotosclone.service.UsersService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;


@RestController
public class PhotosController {
    private final PhotosService photosService;
    private final UsersService usersService;

    private Long requireUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return userId;
    }
    private Photo getPhoto (Long id) {
        Optional<Photo> photo =  photosService.getPhoto(id);
        if (photo.isEmpty()) throw new ResponseStatusException(NOT_FOUND);
        return photo.orElse(null);
    }

    public PhotosController(PhotosService photosService, UsersService usersService) {
        this.photosService = photosService;
        this.usersService = usersService;
    }
   @GetMapping("/api/photos")
   public ResponseEntity<?> photos(HttpSession session) {
        Long userId = requireUserId(session);
        var photos = photosService.getMediaByUserid(userId);
        var result = new ArrayList<PhotoListDto>();
        for (Photo photo : photos) {
            result.add(
                    new PhotoListDto(
                            photo.getId(),
                            photo.getFileName(),
                            photo.getContentType(),
                            "/api/photos/" + photo.getId()
                    )
            );
        }
        return ResponseEntity.ok(result);
   }
   @GetMapping("/api/photos/{id}")
   public ResponseEntity<byte[]> openFile(@PathVariable Long id, HttpSession session) {
        Long userId = requireUserId(session);
        Optional<Photo> photo = photosService.getPhoto(id);
        if (photo.isEmpty() || !photo.get().getUser().getId().equals(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().header("Content-Type", photo.get().getContentType()).body(photo.get().getData());
   }
   @PostMapping("/api/logout")
   public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Session invalidated"));
   }

    @PostMapping("/api/photos")
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        if (!file.isEmpty()) {
            Photo photo = new Photo();
            photo.setUser(usersService.getUser(requireUserId(session)));
            photo.setData(file.getBytes());
            photo.setContentType(file.getContentType());
            photo.setFileName(file.getOriginalFilename());
            photosService.savePhoto(photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto(photo.getFileName(), photo.getContentType()));
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    @DeleteMapping("/api/photos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        Long userId = requireUserId(session);
        Photo photo = getPhoto(id);
        if (!photo.getUser().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        photosService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/api/photos/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, HttpSession session) {
        Long userId = requireUserId(session);
        Photo photo = getPhoto(id);
        if (!photo.getUser().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        byte[] data = photo.getData();
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
}