package uz.aytjanov.dropnest.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.aytjanov.dropnest.entity.UserEntity;
import uz.aytjanov.dropnest.repository.UsersRepository;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(String username, String rawPassword) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setRole("USER");
        userEntity.setPassword(passwordEncoder.encode(rawPassword));
        usersRepository.save(userEntity);
    }

    public boolean isUserExist(String username) {
        return usersRepository.findByUsername(username) != null;
    }
}