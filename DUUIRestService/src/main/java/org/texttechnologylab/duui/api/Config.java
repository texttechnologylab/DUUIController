package org.texttechnologylab.duui.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * A class holding configuration data for the application and allowing for easy access to the configuration.
 *
 * @author Cedric Borkowski
 */
public class Config {

    private final Properties properties;

    public Config(String configPath) throws IOException {
        properties = new Properties();
        try (InputStream input = new FileInputStream(Paths.get(configPath).toAbsolutePath().toString())) {
            properties.load(input);
        }
    }

    public String getMongoDBConnectionString() {
        return properties.getProperty("MONGO_DB_CONNECTION_STRING", null);
    }

    public String getMongoHost() {
        return properties.getProperty("MONGO_HOST", null);
    }

    public int getMongoPort() {
        return Integer.parseInt(properties.getProperty("MONGO_PORT", "27017"));
    }

    public String getMongoUser() {
        return properties.getProperty("MONGO_USER", null);
    }

    public String getMongoDatabase() {
        return properties.getProperty("MONGO_DB", null);
    }

    public String getMongoPassword() {
        return properties.getProperty("MONGO_PASSWORD", null);
    }

    public String getDropboxKey() {
        return properties.getProperty("DBX_APP_KEY", null);
    }

    public String getDropboxSecret() {
        return properties.getProperty("DBX_APP_SECRET", null);
    }

    public String getDropboxRedirectUrl() {
        return properties.getProperty("DBX_REDIRECT_URL", null);
    }

    public List<String> getAllowedOrigins() {
        return List.of(properties.getProperty("ALLOWED_ORIGINS", "").split(";"));
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("PORT", null));
    }

    public String getHost() {
        return properties.getProperty("HOST", "localhost");
    }

    public String getFileUploadPath() {
        return properties.getProperty("FILE_UPLOAD_DIRECTORY", "files/upload");
    }

}
