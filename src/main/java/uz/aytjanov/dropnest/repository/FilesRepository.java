package uz.aytjanov.dropnest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.aytjanov.dropnest.entity.FileRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilesRepository extends JpaRepository<FileRecord, Long> {
    List<FileRecord> findByOwnerId(Long userId);
    Optional<FileRecord> findById(Long id);
    void removeById(Long id);
    Page<FileRecord> findByOriginalNameContainingIgnoreCaseAndOwnerId(String search, Pageable pageable, Long userId);
    Page<FileRecord> findAllByOwnerId(Pageable pageable, Long userId);
}