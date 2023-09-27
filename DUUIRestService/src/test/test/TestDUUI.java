import api.services.DUUIDropboxService;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDUUI {

    private static final String user = System.getenv("mongo_user");
    private static final String pass = System.getenv("mongo_pass");

    private static String getConnectionURI() {
        return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
                        "<user>",
                        user
                )
                .replace("<password>", pass);
    }

    @Test
    public void TestUploadFileDbx() throws FileNotFoundException {
        DUUIDropboxService service = new DUUIDropboxService();
        FileMetadata metadata = service.uploadFile(
                new FileInputStream(
                        "D:\\Uni Informatik B.sc\\Bachelor\\DUUIController\\DUUIRestService\\src\\main\\resources\\test_data.txt"),
                "/lul.txt"
        );
        assertNotNull(metadata);
    }

    @Test
    public void TestListFilesDbx() throws DbxException {
        DUUIDropboxService service = new DUUIDropboxService();
        service.listFiles("");
    }

    @Test
    public void TestDownloadFileDbx() throws DbxException {
        DUUIDropboxService service = new DUUIDropboxService();
        service.downloadFile("/CV.pdf", "/CV2.pdf");
    }

    @Test
    public void TestAuthorizedDownloadFileDbx() throws DbxException {
        String accessToken = System.getenv("dbx_access");
        DUUIDropboxService service = new DUUIDropboxService(accessToken);
        service.downloadFile("/CV.pdf", "/CV2.pdf");
    }

    @Test
    public void TestAuthorizedListFilesDbx() throws DbxException {
        String accessToken = System.getenv("dbx_access");
        DUUIDropboxService service = new DUUIDropboxService(accessToken);

        service.listFiles("");
    }

    @Test
    public void TestAuthorizedFileUploadDbx() throws IOException {

        String accessToken = System.getenv("dbx_access");
        DUUIDropboxService.uploadFileAuthorized(
                accessToken,
                new FileInputStream("D:\\Uni Informatik B.sc\\Bachelor\\DUUIController\\DUUIRestService\\src\\main\\resources\\test_data.txt"),
                "/test.txt"
        );

    }


}
