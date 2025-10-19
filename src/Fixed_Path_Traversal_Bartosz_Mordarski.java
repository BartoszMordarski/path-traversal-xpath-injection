import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class Fixed_Path_Traversal_Bartosz_Mordarski implements VulnerabilityLogic {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Override
    public String process(String userInput, EnvironmentContext context) throws Exception {
        try {
            // 1. Walidacja wejścia
            if (!isInputValid(userInput)) {
                logAttempt(userInput, context.getUserId(), "Nieprawidłowe znaki w ścieżce");
                return "[BLOCKED] Nieprawidłowa ścieżka";
            }

            // 2. Pobranie bezpiecznego katalogu
            Path safeDir = context.getSafeDirectory();
            if (!Files.exists(safeDir) || !Files.isDirectory(safeDir)) {
                return "[ERROR] Katalog bazowy nie istnieje";
            }

            // 3. Uzyskanie rzeczywistej ścieżki katalogu bazowego (rozwija symlinki)
            Path realSafeDir = safeDir.toRealPath();

            // 4. Bezpieczne utworzenie docelowej ścieżki
            Path targetPath = buildSecurePath(realSafeDir, userInput);

            // 5. Weryfikacja bezpieczeństwa
            if (!isPathSafe(targetPath, realSafeDir)) {
                logAttempt(userInput, context.getUserId(), "Próba dostępu poza katalog");
                return "[BLOCKED] Dostęp zabroniony";
            }

            // 6. Sprawdzenie istnienia i typu pliku
            if (!Files.exists(targetPath)) {
                return "[ERROR] Plik nie istnieje";
            }

            if (!Files.isRegularFile(targetPath)) {
                return "[BLOCKED] Można odczytać tylko zwykłe pliki";
            }

            // 7. Sprawdzenie rozmiaru
            long fileSize = Files.size(targetPath);
            if (fileSize > MAX_FILE_SIZE) {
                return "[BLOCKED] Plik zbyt duży: " + fileSize + " bajtów";
            }

            // 8. Bezpieczny odczyt
            String content = readFileSafely(targetPath);
            return "[SUCCESS] Zawartość pliku:\n" + content;

        } catch (SecurityException e) {
            logAttempt(userInput, context.getUserId(), e.getMessage());
            return "[BLOCKED] Naruszenie bezpieczeństwa";
        } catch (IOException e) {
            return "[ERROR] Błąd odczytu pliku";
        }
    }

    // Walidacja wejścia - sprawdza niebezpieczne sekwencje
    private boolean isInputValid(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        if (input.length() > 200) {
            return false;
        }

        String lowerInput = input.toLowerCase();
        String[] forbidden = {
                "..", "~", "\0", "%00", "%2e", "%252e",
                "../", "..\\", "/..", "\\.."
        };

        for (String pattern : forbidden) {
            if (lowerInput.contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    // Bezpieczna konstrukcja ścieżki z normalizacją
    private Path buildSecurePath(Path baseDir, String userInput) throws IOException {
        String cleaned = userInput.trim();
        cleaned = cleaned.replace('\\', '/');
        cleaned = cleaned.replaceAll("^/+", "");
        cleaned = cleaned.replaceAll("/+", "/");

        Path combined = baseDir.resolve(cleaned).normalize();

        if (Files.exists(combined)) {
            combined = combined.toRealPath();
        }

        return combined;
    }

    // Sprawdzenie czy ścieżka jest bezpieczna
    private boolean isPathSafe(Path requestedPath, Path safeDir) throws IOException {
        if (!requestedPath.startsWith(safeDir)) {
            return false;
        }

        if (Files.isSymbolicLink(requestedPath)) {
            Path linkTarget = Files.readSymbolicLink(requestedPath);
            Path resolvedTarget = safeDir.resolve(linkTarget).normalize();

            return resolvedTarget.startsWith(safeDir);
        }

        return true;
    }

    // Bezpieczny odczyt pliku
    private String readFileSafely(Path filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);

        for (byte b : bytes) {
            if (b == 0) {
                throw new IOException("Wykryto plik binarny");
            }
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    // Logowanie prób ataku
    private void logAttempt(String input, String userId, String reason) {
        System.err.println(String.format(
                "[SECURITY WARNING] User: %s | Input: %s | Reason: %s",
                userId, input, reason
        ));
    }
}