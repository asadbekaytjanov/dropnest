package uz.aytjanov.dropnest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.aytjanov.dropnest.entity.UserEntity;
import uz.aytjanov.dropnest.repository.UsersRepository;

@Service
@RequiredArgsConstructor
public class UsersService {
    public static final long TOTAL_QUOTA_BYTES = 50L * 1024L * 1024L;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(String username, String rawPassword) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setRole("USER");
        userEntity.setRemainingStorageBytes(TOTAL_QUOTA_BYTES);
        userEntity.setPassword(passwordEncoder.encode(rawPassword));
        usersRepository.save(userEntity);
    }

    public boolean isUserExist(String username) {
        return usersRepository.findByUsername(username) != null;
    }

    public UserEntity findById(Long userId) {
        return usersRepository.findUserById(userId);
    }
}