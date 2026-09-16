package uz.aytjanov.dropnest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import uz.aytjanov.dropnest.dto.LoginRequestDto;
import uz.aytjanov.dropnest.security.JwtUtils;
import uz.aytjanov.dropnest.service.UsersService;
import java.util.Map;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
public class UsersController {
   private final UsersService usersService;
   private final JwtUtils jwtUtils;
   private final AuthenticationManager authenticationManager;
    public UsersController(UsersService usersService, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.usersService = usersService;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

   @PostMapping("/signup")
   public ResponseEntity<Map<String, Object>> createUser(@RequestParam String username, @RequestParam String password) {
       if (usersService.isUserExist(username)) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "The username is taken. Try another one"));
       } else {
           usersService.createUser(username, password);
           return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", username));
       }
   }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
           @Valid @RequestBody LoginRequestDto request
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", userDetails.getUsername()
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }
}