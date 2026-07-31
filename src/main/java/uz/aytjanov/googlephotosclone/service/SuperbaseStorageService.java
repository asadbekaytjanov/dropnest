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

@Service
public class SuperbaseStorageService {
    private final UsersRepository usersRepository;
    private final PhotosRepository photosRepository;
    private final RestClient restClient = RestClient.create();

    public SuperbaseStorageService(UsersRepository usersRepository, PhotosRepository photosRepository) {
        this.usersRepository = usersRepository;
        this.photosRepository = photosRepository;
    }

    public Photo uploadFile(Long userId, MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadUrl = System.getenv("SUPABASE_URL") + "/storage/v1/object/photos/" + fileName;

        String UPLOAD_DIRECTORY = "uploads/";
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        restClient.post()
                .uri(uploadUrl)
                .header("Authorization", "Bearer " + System.getenv("SUPABASE_KEY"))
                .header("apikey", System.getenv("SUPABASE_KEY"))
                .contentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())))
                .body(file.getBytes())
                .retrieve()
                .toBodilessEntity();
        String publicUrl = System.getenv("SUPABASE_URL") + "/storage/v1/object/public/photos/" + fileName;
        Photo photo = new Photo();
        photo.setUser(usersRepository.findUserById(userId));
        photo.setContentType(file.getContentType());
        photo.setFileName(file.getOriginalFilename());
        photo.setFilePath(publicUrl);
        return photosRepository.save(photo);
    }
}
