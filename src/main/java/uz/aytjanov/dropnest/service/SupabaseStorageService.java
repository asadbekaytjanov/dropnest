package uz.aytjanov.dropnest.service;

import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import uz.aytjanov.dropnest.dto.ResponseDto;
import uz.aytjanov.dropnest.entity.FileRecord;
import uz.aytjanov.dropnest.entity.UserEntity;
import uz.aytjanov.dropnest.repository.FilesRepository;
import uz.aytjanov.dropnest.repository.UsersRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
            throw new IllegalArgumentException(
                    "User not found."
            );
        }

        long fileSize = file.getSize();
        long remainingBytes = user.getRemainingStorageBytes();

        if (fileSize > remainingBytes) {
            throw new IllegalArgumentException(
                    "Your personal storage quota has been exceeded."
            );
        }

        String originalFilename =
                Objects.requireNonNull(
                        file.getOriginalFilename()
                );

        String storedFilename =
                UUID.randomUUID() + "_" + originalFilename;

        String objectKey =
                userId + "/" + storedFilename;

        String supabaseUrl = requireEnvironmentVariable(
                "SUPABASE_URL"
        );

        String supabaseKey = requireEnvironmentVariable(
                "SUPABASE_KEY"
        );

        String uploadUrl =
                supabaseUrl
                        + "/storage/v1/object/files/"
                        + objectKey;
        try {
            restClient.post()
                    .uri(uploadUrl)
                    .header(
                            "Authorization",
                            "Bearer " + supabaseKey
                    )
                    .header("apikey", supabaseKey)
                    .contentType(
                            MediaType.parseMediaType(
                                    Objects.requireNonNull(
                                            file.getContentType()
                                    )
                            )
                    )
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Supabase rejected the upload. "
                            + "The application storage limit may have been reached.",
                    exception
            );
        }

        try {
            FileRecord fileEntity = new FileRecord();

            fileEntity.setOwner(user);
            fileEntity.setContentType(file.getContentType());
            fileEntity.setOriginalName(originalFilename);
            fileEntity.setStoredName(storedFilename);
            fileEntity.setStoragePath(objectKey);
            fileEntity.setSizeBytes(fileSize);
            fileEntity.setCreatedAt(LocalDateTime.now());

            filesRepository.save(fileEntity);

            user.setRemainingStorageBytes(
                    remainingBytes - fileSize
            );

            usersRepository.save(user);

            return new ResponseDto(
                    fileEntity.getOriginalName(),
                    fileEntity.getContentType()
            );

        } catch (Exception exception) {

            try {
                deleteFromSupabase(objectKey);
            } catch (Exception cleanupException) {
                // Log this in a real application.
            }

            throw new IllegalStateException(
                    "The file was uploaded, but metadata could not be saved.",
                    exception
            );
        }
    }

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