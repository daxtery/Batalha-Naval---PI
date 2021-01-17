package Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static Client.PlayerCode.generate;

public class PlayerSettings {

    private static final Path PATH = Paths.get(System.getenv("APPDATA") + "/BB");

    private final String code;
    private final boolean isBot;
    private String name;

    private PlayerSettings(String code, String name, boolean isBot) {
        this.code = code;
        this.isBot = isBot;
        this.name = name;
    }

    private PlayerSettings(String code) {
        this(code, null, false);
    }

    private PlayerSettings(String code, String name) {
        this(code, name, true);
    }

    public static PlayerSettings loadOrCreateWithCode() throws IOException {
        if (Files.exists(PATH)) {
            List<String> lines = Files.readAllLines(PATH);
            assert lines.size() > 0;
            String code = lines.get(0);
            return new PlayerSettings(code);
        }

        String code = generate();

        Files.createFile(PATH);
        Files.writeString(PATH, code);

        return new PlayerSettings(code);
    }

    public static PlayerSettings botConfiguration(String name) throws IOException {
        return new PlayerSettings("BOT " + name, name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public boolean isBot() {
        return isBot;
    }
}
