package uz.aytjanov.googlephotosclone.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.repository.PhotosRepository;
import uz.aytjanov.googlephotosclone.repository.UsersRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

@Service
public class SuperbaseStorageService {
    private final UsersRepository usersRepository;
    private final PhotosRepository photosRepository;
    private final RestClient restClient = RestClient.create();
    public SuperbaseStorageService(UsersRepository usersRepository, PhotosRepository photosRepository) {
        this.usersRepository = usersRepository;
        this.photosRepository = photosRepository;
    }
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "video/mp4",
            "video/quicktime",
            "image/jpeg",
            "image/png"
    );

    public void validateFile (MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty. Please upload a valid file.");
        long maxBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxBytes) throw new IllegalArgumentException("File exceeds the 5MB limit.");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType))
            throw new IllegalArgumentException("Only photos and videos are allowed");
    }
    public Photo uploadFile(Long userId, MultipartFile file) throws IOException {
        validateFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePathInBucket = userId + "/" + fileName;
        String uploadUrl = System.getenv("SUPABASE_URL") + "/storage/v1/object/photos/" + filePathInBucket;
        restClient.post()
                .uri(uploadUrl)
                .header("Authorization", "Bearer " + System.getenv("SUPABASE_KEY"))
                .header("apikey", System.getenv("SUPABASE_KEY"))
                .contentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())))
                .body(file.getBytes())
                .retrieve()
                .toBodilessEntity();
        String publicUrl = System.getenv("SUPABASE_URL") + "/storage/v1/object/public/photos/" + filePathInBucket;
        Photo photo = new Photo();
        photo.setUser(usersRepository.findUserById(userId));
        photo.setContentType(file.getContentType());
        photo.setFileName(file.getOriginalFilename());
        photo.setFilePath(publicUrl);
        return photosRepository.save(photo);
    }
}
