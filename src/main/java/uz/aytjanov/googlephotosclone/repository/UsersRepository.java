package uz.aytjanov.googlephotosclone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.aytjanov.googlephotosclone.entity.User;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findById(Long id);
}