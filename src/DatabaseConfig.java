import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConfig {
    private String url;
    private String user;
    private String password;

    public DatabaseConfig(String propertiesFilePath) throws IOException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(propertiesFilePath)) {
            props.load(in);
            this.url = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.password = props.getProperty("db.password");
        }
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
}
