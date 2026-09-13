package uz.aytjanov.dropnest.dto;

public record FileListDto(
        Long id,
        String fileName,
        String contentType
) {}