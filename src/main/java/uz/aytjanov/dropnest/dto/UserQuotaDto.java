package uz.aytjanov.dropnest.dto;

public record UserQuotaDto(
        long totalQuotaBytes,
        long remainingStorageBytes,
        long usedStorageBytes,
        int usedPercentage
) {
}
