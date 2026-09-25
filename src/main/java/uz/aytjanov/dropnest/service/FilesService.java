package uz.aytjanov.dropnest.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.dropnest.entity.FileRecord;
import uz.aytjanov.dropnest.repository.FilesRepository;

import java.util.Objects;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FilesService {

    private final FilesRepository filesRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final RestClient restClient = RestClient.create();

    public FilesService(
            FilesRepository filesRepository,
            SupabaseStorageService supabaseStorageService
    ) {
        this.filesRepository = filesRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    public FileRecord getFile(Long id, Long userId) {

        FileRecord fileRecord =
                filesRepository.findByIdAndOwnerId(id, userId);

        if (fileRecord == null) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "File not found"
            );
        }

        return fileRecord;
    }

    public ResponseEntity<byte[]> download(
            Long id,
            Long userId
    ) {
        FileRecord fileRecord = getFile(id, userId);

        if (fileRecord.getOwner() == null
                || !fileRecord.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "You cannot access this file"
            );
        }

        byte[] data =
                readBytesFromStoragePath(
                        fileRecord.getStoragePath()
                );

        MediaType mediaType =
                resolveMediaType(fileRecord.getContentType());

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(mediaType);

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileRecord.getOriginalName())
                        .build()
        );

        return new ResponseEntity<>(
                data,
                headers,
                HttpStatus.OK
        );
    }

    public Page<FileRecord> searchFilesByUserId(
            String search,
            Pageable pageable,
            Long userId
    ) {
        if (search == null || search.isBlank()) {
            return filesRepository.findAllByOwnerId(
                    pageable,
                    userId
            );
        }

        return filesRepository
                .findByOriginalNameContainingIgnoreCaseAndOwnerId(
                        search,
                        pageable,
                        userId
                );
    }

    @Transactional
    public void delete(Long id, Long userId) {

        FileRecord fileRecord = getFile(id, userId);

        if (fileRecord.getOwner() == null
                || !fileRecord.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Forbidden"
            );
        }
        supabaseStorageService.deleteObject(fileRecord.getStoragePath(), userId, fileRecord.getSizeBytes());
        filesRepository.delete(fileRecord);
    }

    private byte[] readBytesFromStoragePath(
            String objectKey
    ) {
        String baseUrl =
                requireEnvironmentVariable("SUPABASE_URL");

        String key =
                requireEnvironmentVariable("SUPABASE_KEY");

        String url =
                baseUrl
                        + "/storage/v1/object/files/"
                        + objectKey;

        return restClient.get()
                .uri(url)
                .header(
                        "Authorization",
                        "Bearer " + key
                )
                .header("apikey", key)
                .retrieve()
                .body(byte[].class);
    }

    private MediaType resolveMediaType(
            String contentType
    ) {
        try {
            if (contentType == null
                    || contentType.isBlank()) {
                return MediaType.APPLICATION_OCTET_STREAM;
            }

            return MediaType.parseMediaType(contentType);

        } catch (Exception exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String requireEnvironmentVariable(
            String name
    ) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    name + " environment variable is missing."
            );
        }

        return value;
    }
}