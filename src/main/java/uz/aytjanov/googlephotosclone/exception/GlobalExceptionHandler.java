package uz.aytjanov.googlephotosclone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // maxUploadSize errors
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> maxUploadSizeException(MaxUploadSizeExceededException exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Max upload size exceeded! Please keep it under 5MB.");
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(response);
    }
    // upload validation errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> illegalArgumentExc(IllegalArgumentException exc) {
        Map<String, String> response =  new HashMap<>();
        response.put("error", exc.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    // internal server errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> exception(Exception exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", exc.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}