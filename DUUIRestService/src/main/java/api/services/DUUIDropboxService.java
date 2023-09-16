package api.services;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadZipResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.*;
import java.util.Objects;

public class DUUIDropboxService {

    private static final String ACCESS_TOKEN = System.getenv("dropbox_token");
    private final DbxRequestConfig config;
    private final DbxClientV2 client;

    public DUUIDropboxService() {
        config = DbxRequestConfig.newBuilder("Cedric Test App").build();
        client = new DbxClientV2(config, ACCESS_TOKEN);
    }

    public DUUIDropboxService(String userAccessToken) {
        config = DbxRequestConfig.newBuilder("Cedric Test App").build();
        client = new DbxClientV2(config, userAccessToken);
    }

    public void listFiles(String dbxFolder) throws DbxException {
        ListFolderResult result = client.files().listFolder(dbxFolder);
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                System.out.println(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
    }

    public FileMetadata uploadFile(FileInputStream inputStream, String dbxDestinationPath) {
        try {
            return client.files().uploadBuilder(dbxDestinationPath).uploadAndFinish(inputStream);
        } catch (DbxException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed. File not found.");
        }
        return null;
    }

    public void uploadFiles(String sourceFolder, String dbxDestinationFolder) throws FileNotFoundException {
        File input = new File(sourceFolder);
        if (!input.isDirectory()) {
            throw new IllegalArgumentException("Input can only be a directory.");
        }

        for (File file : Objects.requireNonNull(input.listFiles())) {
            if (!file.isFile()) continue;
            uploadFile(new FileInputStream(file), dbxDestinationFolder + "/" + file.getName());
        }
    }

    public void downloadFile(String dbxSource, String destination) throws DbxException {
        DbxDownloader<FileMetadata> downloader = client.files().download(dbxSource);
        try {
            FileOutputStream out = new FileOutputStream(destination);
            downloader.download(out);
            out.close();
        } catch (DbxException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadFiles(String dbxSourceFolder, String destinationFolder) throws DbxException {
        DbxDownloader<DownloadZipResult> downloader = client.files().downloadZip(dbxSourceFolder);
        try {
            FileOutputStream out = new FileOutputStream(destinationFolder);
            downloader.download(out);
            out.close();
        } catch (DbxException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DbxAuthFinish authorize(String appKey) throws IOException {
        DbxPKCEWebAuth pkceWebAuth = new DbxPKCEWebAuth(config, new DbxAppInfo(appKey));
        DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
                .withNoRedirect()
                .build();

        String authorizeUrl = pkceWebAuth.authorize(webAuthRequest);
        System.out.println("1. Go to " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first).");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (code == null) {
            System.exit(1);
        }
        code = code.trim();
        try {
            // You must use the same DbxPKCEWebAuth to generate authorizationUrl and to handle code
            // exchange.
            return pkceWebAuth.finishFromCode(code);
        } catch (DbxException ex) {
            System.err.println("Error in DbxWebAuth.authorize: " + ex.getMessage());
            System.exit(1);
            return null;
        }
    }

    public static void uploadFileAuthorized(String accessToken, FileInputStream inputStream, String dbxDestinationPath) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("Cedric Test App").build();
        DbxClientV2 client = new DbxClientV2(config, accessToken);
        try {
            FileMetadata metadata = client.files().uploadBuilder(dbxDestinationPath).uploadAndFinish(inputStream);
            System.out.println("File uploaded to Dropbox: " + metadata.getName());
        } catch (DbxException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed. File not found.");
        }
    }


    public static void main(String[] args) throws DbxException, IOException {

        DUUIDropboxService service = new DUUIDropboxService("Cedric Test App");
        System.out.println(service.authorize(System.getenv("dbx_app_key")).getAccessToken());
//        downloadFiles("/sample_splitted", "C:\\Users\\Cedric\\OneDrive\\Desktop\\out.zip");
//        listFiles("/sample");
//        uploadFiles("D:\\Uni Informatik B.sc\\Bachelor\\DockerUnifiedUIMAInterface-Fork\\src\\main\\resources\\sample_splitted", "/sample_splitted");
//        uploadFile(new FileInputStream("DUUIRestService/src/main/resources/test_data.txt"), "/test.txt");
    }
}
