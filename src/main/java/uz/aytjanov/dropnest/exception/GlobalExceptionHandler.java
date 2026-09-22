package uz.aytjanov.dropnest.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import uz.aytjanov.dropnest.dto.ExceptionDto;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // maxUploadSize errors
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionDto> maxUploadSizeException(MaxUploadSizeExceededException exc, HttpServletRequest request) {
        ExceptionDto response = new ExceptionDto(
                HttpStatus.CONTENT_TOO_LARGE.value(),
                HttpStatus.CONTENT_TOO_LARGE.name(),
                "Max upload size exceeded! Please keep it under 50MB.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(response);
    }
    // upload validation errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDto> illegalArgumentExc(IllegalArgumentException exc, HttpServletRequest request) {
        ExceptionDto response = new ExceptionDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                "Your storage quota has been exceeded.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionDto> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = ex.getStatusCode();
        int status = statusCode.value();

        String error = (statusCode instanceof HttpStatus hs) ? hs.name() : "ERROR";
        String message = ex.getReason() != null ? ex.getReason() : "Request failed";
        ExceptionDto response = new ExceptionDto(
            status,
            error,
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(statusCode).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> handleGenericException(HttpServletRequest request) {
        ExceptionDto response = new ExceptionDto(
                 500,
                 "INTERNAL_SERVER_ERROR",
                 "Unexpected server error",
                 request.getRequestURI()
        );

        return ResponseEntity.status(500).body(response);
    }
}