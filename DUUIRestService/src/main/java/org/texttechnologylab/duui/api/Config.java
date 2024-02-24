package org.texttechnologylab.duui.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

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
