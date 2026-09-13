package uz.aytjanov.dropnest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.aytjanov.dropnest.entity.UserEntity;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsername(String username);
    UserEntity findUserById(Long id);
}