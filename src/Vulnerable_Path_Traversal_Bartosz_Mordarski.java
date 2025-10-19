import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class Vulnerable_Path_Traversal_Bartosz_Mordarski implements VulnerabilityLogic {

    @Override
    public String process(String userInput, EnvironmentContext context) throws Exception {
        // PODATNOŚĆ: brak jakiejkolwiek walidacji wejścia użytkownika
        Path baseDir = context.getSafeDirectory();

        // Bezpośrednie połączenie ścieżki - umożliwia atak path traversal
        Path targetPath = baseDir.resolve(userInput);

        // Sprawdzenie czy plik istnieje (ale bez weryfikacji czy jest w dozwolonym katalogu)
        if (!Files.exists(targetPath)) {
            return "[ERROR] Plik nie istnieje: " + userInput;
        }

        // Odczyt zawartości bez żadnych zabezpieczeń
        byte[] fileContent = Files.readAllBytes(targetPath);
        String content = new String(fileContent, StandardCharsets.UTF_8);

        return "[SUCCESS] Zawartość pliku:\n" + content;
    }
}