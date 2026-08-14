package uz.aytjanov.googlephotosclone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.aytjanov.googlephotosclone.service.UsersService;
import java.util.Map;

@RestController
public class UsersController {
   private final UsersService usersService;

   public UsersController(UsersService usersService) {
        this.usersService = usersService;
   }
   @PostMapping("/api/register")
   public ResponseEntity<?> createUser(@RequestParam String username, @RequestParam String password) {
       if (usersService.isUserExist(username)) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "The username is taken. Try another one"));
       } else {
           usersService.createUser(username, password);
           return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", username));
       }
   }
}
