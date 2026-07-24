package uz.aytjanov.googlephotosclone.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uz.aytjanov.googlephotosclone.entity.Photo;
import uz.aytjanov.googlephotosclone.repository.PhotosRepository;

import java.util.Optional;

@Service
public class PhotosService {
    private final PhotosRepository photosRepository;

    public PhotosService(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }

    public void savePhoto(Photo photo) {
        photosRepository.save(photo);
    }
    public Iterable<Photo> getMediaByUserid(Long userId) {
       return photosRepository.findByUserId(userId);
    }
    @Transactional
    public void delete(Long id) {
        photosRepository.removeById(id);
    }
    public Optional<Photo> getPhoto(Long id) {
        return photosRepository.findById(id);
    }
}