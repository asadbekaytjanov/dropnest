package uz.aytjanov.dropnest.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.dropnest.dto.FileListItemDto;
import uz.aytjanov.dropnest.dto.ResponseDto;
import uz.aytjanov.dropnest.entity.CustomUserDetails;
import uz.aytjanov.dropnest.entity.FileRecord;
import uz.aytjanov.dropnest.service.FilesService;
import uz.aytjanov.dropnest.service.SupabaseStorageService;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FilesController {

    private final FilesService filesService;
    private final SupabaseStorageService supabaseStorageService;

    public FilesController(FilesService filesService, SupabaseStorageService supabaseStorageService) {
        this.filesService = filesService;
        this.supabaseStorageService = supabaseStorageService;
    }

    private Long requireUserId(CustomUserDetails currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return currentUser.getId();
    }

    @GetMapping
    public ResponseEntity<Page<FileListItemDto>> files(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Long userId = requireUserId(currentUser);
        if (size > 100) size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<FileListItemDto> result = filesService.searchFilesByUserId(search, pageable, userId)
                .map(f -> new FileListItemDto(
                        f.getId(),
                        f.getOriginalName(),
                        f.getContentType(),
                        f.getSizeBytes()
                ));

        return ResponseEntity.ok(result);
    }
    @PostMapping
    public ResponseEntity<ResponseDto> create(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws IOException {

        Long userId = requireUserId(currentUser);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        FileRecord fileRecord = supabaseStorageService.uploadFile(userId, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(fileRecord.getOriginalName(), fileRecord.getContentType()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Long userId = requireUserId(currentUser);
        FileRecord fileRecord = filesService.getFile(id, userId);
        if (fileRecord == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        filesService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws IOException {
        Long userId = requireUserId(currentUser);
        return filesService.download(id, userId);
    }
}