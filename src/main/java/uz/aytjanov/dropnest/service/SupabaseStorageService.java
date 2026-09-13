package uz.aytjanov.dropnest.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import uz.aytjanov.dropnest.entity.FileRecord;
import uz.aytjanov.dropnest.repository.FilesRepository;
import uz.aytjanov.dropnest.repository.UsersRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Service
public class SupabaseStorageService {
    private final UsersRepository usersRepository;
    private final FilesRepository filesRepository;
    private final RestClient restClient = RestClient.create();
    public SupabaseStorageService(UsersRepository usersRepository, FilesRepository filesRepository) {
        this.usersRepository = usersRepository;
        this.filesRepository = filesRepository;
    }
    private static final Set<String> ALLOWED_TYPES = Set.of(
            // Images
            "image/jpeg", "image/png", "image/webp", "image/gif",

            // Video
            "video/mp4", "video/webm", "video/quicktime",

            // Audio
            "audio/mpeg", "audio/wav", "audio/ogg",

            // Documents
            "application/pdf",
            "text/plain",
            "text/csv",
            "application/json",
            "application/xml",
            "text/xml",

            // Office
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",

            // Archives
            "application/zip",
            "application/x-7z-compressed",
            "application/x-rar-compressed",
            "application/gzip"
    );

    public void validateFile (MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty. Please upload a valid file.");
        long maxBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxBytes) throw new IllegalArgumentException("File exceeds the 5MB limit.");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType))
            throw new IllegalArgumentException("This type is not allowed!");
    }
    public FileRecord uploadFile(Long userId, MultipartFile file) throws IOException {
        validateFile(file);
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
        fileEntity.setOwner(usersRepository.findUserById(userId));
        fileEntity.setContentType(file.getContentType());
        fileEntity.setOriginalName(file.getOriginalFilename());
        fileEntity.setStoredName(file.getName());
        fileEntity.setStoragePath(objectKey);
        fileEntity.setStoredName(fileName);
        fileEntity.setSizeBytes(file.getSize());
        fileEntity.setCreatedAt(LocalDateTime.now());
        return filesRepository.save(fileEntity);
    }
    public void deleteObject(String objectKey) {
        String baseUrl = System.getenv("SUPABASE_URL");
        String key = System.getenv("SUPABASE_KEY");

        String deleteUrl = baseUrl + "/storage/v1/object/files/" + objectKey;

        restClient.delete()
                .uri(deleteUrl)
                .header("Authorization", "Bearer " + key)
                .header("apikey", key)
                .retrieve()
                .toBodilessEntity();
    }

}
