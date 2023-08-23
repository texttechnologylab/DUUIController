package DUUIDockerService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.json.JSONObject;
import org.openjdk.nashorn.internal.parser.JSONParser;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.xml.sax.SAXException;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URISyntaxException;

public class DUUIDockerService {

    private DUUIComposer composer;
    private DUUIDockerDriver driver;
    private String _processIdentifier;
    private final JCas document;

    public DUUIDockerService() throws UIMAException, IOException, SAXException {
        document = JCasFactory.createJCas();
    }

    public static Object build(Request request, Response response) throws IOException, URISyntaxException, UIMAException, SAXException {
        DUUIDockerService service = new DUUIDockerService();

        try {
            ObjectMapper mapper = new ObjectMapper();
            DUUIDockerComponentPayload componentPayload = mapper.readValue(request.body(), DUUIDockerComponentPayload.class);
            System.out.println(componentPayload.getTimeout());
            System.out.println(componentPayload.getImageURL());
            response.status(200);

        } catch (JsonParseException e) {
            response.status(400);
        }


        return "Docker";
    }

    public DUUIDockerService withTimeout(int timeoutMilliseconds) {
        System.out.println(timeoutMilliseconds);
//        driver.withTimeout(timeoutMilliseconds);
        return this;
    }


    public void startComposer(String processIdentifier) throws Exception {
        _processIdentifier = processIdentifier;
        composer.run(document, processIdentifier);
    }

    public String getIdentifier() {
        return _processIdentifier;
    }

}

