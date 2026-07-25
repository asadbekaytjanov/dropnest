package uz.aytjanov.googlephotosclone.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.aytjanov.googlephotosclone.dto.PhotoListDto;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.repository.PhotosRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PhotosService {
    private final PhotosRepository photosRepository;
    private final String UPLOAD_DIRECTORY = "uploads/";
    public PhotosService(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }

    public void savePhoto(Photo photo) {
        photosRepository.save(photo);
    }
    public List<Photo> getMediaByUserid(Long userId) {
       return photosRepository.findByUserId(userId);
    }

    public Photo createAndSave(Long userId, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String uniqueFileName = UUID.randomUUID().toString() + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        file.transferTo(filePath);
        Photo photo = new Photo();
        photo.setContentType(file.getContentType());
        photo.setFileName(file.getOriginalFilename());
        photo.setFilePath(filePath.toString());
        return photo;
    }
    @Transactional
    public void delete(Long id) {
        photosRepository.removeById(id);
    }
    public Optional<Photo> getPhoto(Long id) {
        return photosRepository.findById(id);
    }

    public List<PhotoListDto> getAllUsersPhotosAsDtos(Long userId) {
        return photosRepository.findByUserId(userId).stream()
                .map(photo -> new PhotoListDto(photo.getId(), photo.getFileName(), photo.getContentType()))
                .toList();
    }
}