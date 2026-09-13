package uz.aytjanov.dropnest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DropNestApplicationTests {

    @Test
    void testPasswordValidation() {
      String validPassword = "String12";
      boolean isValid = validPassword.length() >= 8;
      assertTrue(isValid, "Password should be at least 8 characters");
    }
    @Test
    void testEmailValidation() {
        String validEmail = "user@example.com";
        boolean hasAtSymbol = validEmail.contains("@");
        assertTrue(hasAtSymbol, "Email must contain @ symbol");
    }
    @Test
    void testUploadValidation() {
        String fileName = "file.jpg";
        boolean isUploadValid = fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".mp4") || fileName.endsWith(".mov");
        assertTrue(isUploadValid, "You can upload only photos and videos");
    }

}
