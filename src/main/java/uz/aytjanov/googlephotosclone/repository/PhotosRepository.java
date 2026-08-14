package uz.aytjanov.googlephotosclone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<Photo> findByFileNameContainingIgnoreCase(String search, Pageable pageable);
    Page<Photo> findAll(Pageable pageable);
}