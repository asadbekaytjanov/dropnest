package uz.aytjanov.googlephotosclone.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.aytjanov.googlephotosclone.entity.Photo;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotosRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByUserId(Long userId);
    Optional<Photo> findById(Long id);
    void removeById(Long id);
}