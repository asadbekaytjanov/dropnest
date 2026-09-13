package uz.aytjanov.dropnest.service;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.aytjanov.dropnest.entity.CustomUserDetails;
import uz.aytjanov.dropnest.entity.UserEntity;
import uz.aytjanov.dropnest.repository.UsersRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;

    public CustomUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity user = usersRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new CustomUserDetails(user);
    }
}
