package uz.aytjanov.dropnest.service;

import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.dropnest.entity.FileRecord;
import uz.aytjanov.dropnest.entity.UserEntity;
import uz.aytjanov.dropnest.repository.FilesRepository;
import uz.aytjanov.dropnest.repository.UsersRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class SupabaseStorageService {

    private final UsersRepository usersRepository;
    private final FilesRepository filesRepository;
    private final RestClient restClient = RestClient.create();

    private static final long MAX_FILE_SIZE =
            5L * 1024L * 1024L;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",

            "video/mp4",
            "video/webm",
            "video/quicktime",

            "audio/mpeg",
            "audio/wav",
            "audio/ogg",

            "application/pdf",
            "text/plain",
            "text/csv",
            "application/json",
            "application/xml",
            "text/xml",

            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",

            "application/zip",
            "application/x-7z-compressed",
            "application/x-rar-compressed",
            "application/gzip"
    );

    public SupabaseStorageService(
            UsersRepository usersRepository,
            FilesRepository filesRepository
    ) {
        this.usersRepository = usersRepository;
        this.filesRepository = filesRepository;
    }

    public void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File is empty. Please upload a valid file."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File exceeds the 50 MiB limit."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "This file type is not allowed."
            );
        }

        if (file.getOriginalFilename() == null
                || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException(
                    "The file must have a name."
            );
        }
    }

    @Transactional
    public ResponseDto uploadFile(
            Long userId,
            MultipartFile file
    ) throws IOException {

        validateFile(file);
        UserEntity user = usersRepository.findUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(BAD_REQUEST, "User not found");
        }

        long currentRemaining = Math.max(0L,
                user.getRemainingStorageBytes() == null ? UsersService.TOTAL_QUOTA_BYTES : user.getRemainingStorageBytes());
        if (file.getSize() > currentRemaining) {
            throw new ResponseStatusException(BAD_REQUEST, "Not enough storage quota remaining.");
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String objectKey = userId + "/" + fileName;
        String uploadUrl = System.getenv("SUPABASE_URL") + "/storage/v1/object/files/" + objectKey;
        restClient.post()
                .uri(uploadUrl)
                .header("Authorization", "Bearer " + System.getenv("SUPABASE_KEY"))
                .header("apikey", System.getenv("SUPABASE_KEY"))
                .contentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())))
                .body(file.getBytes())
                .retrieve()
                .toBodilessEntity();
        FileRecord fileEntity = new FileRecord();
        fileEntity.setOwner(user);
        fileEntity.setContentType(file.getContentType());
        fileEntity.setOriginalName(file.getOriginalFilename());
        fileEntity.setStoredName(file.getName());
        fileEntity.setStoragePath(objectKey);
        fileEntity.setStoredName(fileName);
        fileEntity.setSizeBytes(file.getSize());
        fileEntity.setCreatedAt(LocalDateTime.now());
        FileRecord savedFileRecord = filesRepository.save(fileEntity);
        user.setRemainingStorageBytes(currentRemaining - savedFileRecord.getSizeBytes());
        usersRepository.save(user);
        return savedFileRecord;
    }
    public void deleteObject(String objectKey, Long userId, long fileSize) {
        String baseUrl = System.getenv("SUPABASE_URL");
        String key = System.getenv("SUPABASE_KEY");

    @Transactional
    public void deleteObject(
            String objectKey,
            Long userId,
            long fileSize
    ) {
        deleteFromSupabase(objectKey);

        UserEntity user = usersRepository.findUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "User not found."
            );
        }

        long updatedRemainingBytes =
                user.getRemainingStorageBytes() + fileSize;

        user.setRemainingStorageBytes(updatedRemainingBytes);

        usersRepository.save(user);
    }

    private void deleteFromSupabase(String objectKey) {

        String supabaseUrl = requireEnvironmentVariable(
                "SUPABASE_URL"
        );

        String supabaseKey = requireEnvironmentVariable(
                "SUPABASE_KEY"
        );

        String deleteUrl =
                supabaseUrl
                        + "/storage/v1/object/files/"
                        + objectKey;

        restClient.delete()
                .uri(deleteUrl)
                .header(
                        "Authorization",
                        "Bearer " + supabaseKey
                )
                .header("apikey", supabaseKey)
                .retrieve()
                .toBodilessEntity();

        UserEntity user = usersRepository.findUserById(userId);
        if (user != null) {
            long currentRemaining = Math.max(0L,
                    user.getRemainingStorageBytes() == null ? UsersService.TOTAL_QUOTA_BYTES : user.getRemainingStorageBytes());
            long updatedRemaining = Math.min(UsersService.TOTAL_QUOTA_BYTES, currentRemaining + fileSize);
            user.setRemainingStorageBytes(updatedRemaining);
            usersRepository.save(user);
        }
    }

    private String requireEnvironmentVariable(String name) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    name + " environment variable is missing."
            );
        }

        return value;
    }
}