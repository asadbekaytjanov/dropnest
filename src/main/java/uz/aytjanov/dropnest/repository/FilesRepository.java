package uz.aytjanov.dropnest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.aytjanov.dropnest.entity.FileRecord;

@Repository
public interface FilesRepository extends JpaRepository<FileRecord, Long> {
    FileRecord findByIdAndOwnerId(Long id, Long userId);
    Page<FileRecord> findByOriginalNameContainingIgnoreCaseAndOwnerId(String search, Pageable pageable, Long userId);
    Page<FileRecord> findAllByOwnerId(Pageable pageable, Long userId);
}