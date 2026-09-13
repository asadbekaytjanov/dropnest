package uz.aytjanov.dropnest.dto;

public record FileListItemDto(
        Long id,
        String originalName,
        String contentType,
        Long sizeBytes
) {}
