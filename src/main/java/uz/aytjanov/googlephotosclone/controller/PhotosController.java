package uz.aytjanov.googlephotosclone.controller;

import com.sun.net.httpserver.Headers;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.ServerRequest;
import uz.aytjanov.googlephotosclone.dto.PhotoListDto;
import uz.aytjanov.googlephotosclone.dto.ResponseDto;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.service.PhotosService;
import uz.aytjanov.googlephotosclone.service.UsersService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
    private boolean isPhotoBelongsToUser(Photo photo, Long userId) {
        if (photo == null) return false;
        return photo.getUser().getId().equals(userId);
    }
    public PhotosController(PhotosService photosService, UsersService usersService) {
        this.photosService = photosService;
        this.usersService = usersService;
    }
   @GetMapping("/api/photos")
   public ResponseEntity<?> photos(HttpSession session) {
        Long userId = requireUserId(session);
        List<PhotoListDto> result = photosService.getAllUsersPhotosAsDtos(userId);
        return ResponseEntity.ok(result);
   }
   @GetMapping("/api/photos/{id}")
   public ResponseEntity<byte[]> openFile(@PathVariable Long id, HttpSession session) throws IOException {
        Long userId = requireUserId(session);
        Photo photo = photosService.getPhoto(id);
        if (!isPhotoBelongsToUser(photo, userId)) return ResponseEntity.notFound().build();
        return photosService.openTheFile(photo);
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
        Long userId = requireUserId(session);
        Photo photo = photosService.createAndSave(userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto(photo.getFileName(), photo.getContentType()));
    }
    @DeleteMapping("/api/photos/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable Long id, HttpSession session) {
        Long userId = requireUserId(session);
        Photo photo = photosService.getPhoto(id);
        if (!photo.getUser().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        photosService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/api/photos/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, HttpSession session) throws IOException {
        Long userId = requireUserId(session);
        return photosService.download(id, userId);
    }
}