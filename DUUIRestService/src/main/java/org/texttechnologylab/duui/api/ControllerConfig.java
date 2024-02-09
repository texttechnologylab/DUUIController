package org.texttechnologylab.duui.api;

import java.io.*;
import java.util.Properties;

public class ControllerConfig extends Properties {

    public ControllerConfig(File pFile) throws IOException {
        this(pFile.getAbsolutePath());
    }

    public ControllerConfig(String sPath) throws IOException {
        BufferedReader lReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sPath)), "UTF-8"));
        this.load(lReader);
        lReader.close();
    }

    public String getDropboxAppKey(){
        return getProperty("DBX_APP_KEY", "");
    }

    public String getDropboxAppSekret(){
        return getProperty("DBX_APP_SECRET", "");
    }

    public String getDropboxRedirectURL(){
        return getProperty("DBX_REDIRECT_URL", "");
    }

    public String getMongoDBConnection(){
        return getProperty("MONGO_DB_CONNECTION_STRING", "");
    }

    public int getPort(){
        return Integer.valueOf(getProperty("PORT", "8080"));
    }

    public String getHostname(){
        return getProperty("PORT", "api.org.texttechnologylab.duui.duui.texttechnologylab.org");
    }


    public String getUploadDirectory(){
        return getProperty("FILE_UPLOAD_DIRECTORY", "/opt/upload/");
    }

}
