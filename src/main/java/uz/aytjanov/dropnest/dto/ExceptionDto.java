package uz.aytjanov.dropnest.dto;

import java.time.Instant;

public record ExceptionDto(
        String timestamp,
        int status,
        String error,
        String message,
        String path
){
    public ExceptionDto(int status, String error, String message, String requestURI) {
        this(Instant.now().toString(), status, error, message, requestURI);
    }
}
