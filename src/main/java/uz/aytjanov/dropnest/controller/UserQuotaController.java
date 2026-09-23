package uz.aytjanov.dropnest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.dropnest.dto.UserQuotaDto;
import uz.aytjanov.dropnest.entity.CustomUserDetails;
import uz.aytjanov.dropnest.entity.UserEntity;
import uz.aytjanov.dropnest.service.UsersService;

@RestController
@RequestMapping("/api/users")
public class UserQuotaController {
    private final UsersService usersService;

    public UserQuotaController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/me/quota")
    public ResponseEntity<UserQuotaDto> getMyQuota(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        UserEntity user = usersService.findById(currentUser.getId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        long totalQuotaBytes = UsersService.TOTAL_QUOTA_BYTES;
        Long storedRemaining = user.getRemainingStorageBytes();
        long remainingStorageBytes = storedRemaining == null
                ? totalQuotaBytes
                : Math.max(0L, Math.min(totalQuotaBytes, storedRemaining));
        long usedStorageBytes = totalQuotaBytes - remainingStorageBytes;
        int usedPercentage = (int) Math.round((usedStorageBytes * 100.0) / totalQuotaBytes);

        return ResponseEntity.ok(new UserQuotaDto(
                totalQuotaBytes,
                remainingStorageBytes,
                usedStorageBytes,
                usedPercentage
        ));
    }
}
