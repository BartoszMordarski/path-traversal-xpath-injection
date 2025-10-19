import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentContext {
    private final Path safeDirectory;
    private final String userId;
    private final Map<String, String> properties;

    public EnvironmentContext(String safeDirPath, String userId) {
        this.safeDirectory = Paths.get(safeDirPath);
        this.userId = userId;
        this.properties = new HashMap<>();
    }

    public Path getSafeDirectory() {
        return safeDirectory;
    }

    public String getUserId() {
        return userId;
    }

    // Nowa metoda do przechowywania i pobierania właściwości
    public void setString(String key, String value) {
        this.properties.put(key, value);
    }

    public String getString(String key) {
        return this.properties.get(key);
    }
}