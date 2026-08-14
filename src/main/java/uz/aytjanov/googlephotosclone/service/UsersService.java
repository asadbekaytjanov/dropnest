package uz.aytjanov.googlephotosclone.service;

import org.springframework.stereotype.Service;
import uz.aytjanov.googlephotosclone.entity.User;
import uz.aytjanov.googlephotosclone.repository.UsersRepository;
import java.util.Optional;


@Service
public class UsersService {
    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public void createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        usersRepository.save(user);
    }

    public boolean isUserExist(String username) {
        return usersRepository.findByUsername(username) != null;
    }
    public Optional<User> getUser(Long id) {
        return usersRepository.findById(id);
    }
    public User authenticate(String username, String password) {
        if (isUserExist(username) && usersRepository.findByUsername(username).getPassword().equals(password)) {
            User user = new User();
            user.setId(usersRepository.findByUsername(username).getId());
            user.setPassword(password);
            user.setUsername(username);
            return user;
        }
        return null;
    }
}