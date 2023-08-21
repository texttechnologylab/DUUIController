package DUUIDockerService;

import DUUIReponse.DUUIStandardResponse;
import DUUIReponse.DUUIStatusResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URISyntaxException;
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

public class DUUIDockerService {

  private DUUIComposer composer;
  private DUUIDockerDriver driver;
  private String _processIdentifier;
  private final JCas document;

  public DUUIDockerService() throws UIMAException, IOException, SAXException {
    document = JCasFactory.createJCas();
  }

  public static Object build(Request request, Response response)
    throws IOException, URISyntaxException, UIMAException, SAXException {

    try {
      ObjectMapper mapper = new ObjectMapper();
      DUUIDockerComponentPayload componentPayload = mapper.readValue(
        request.body(),
        DUUIDockerComponentPayload.class
      );
      System.out.println(componentPayload.getTimeout());
      System.out.println(componentPayload.getImageURL());
      System.out.println(componentPayload.getScale());
      response.status(200);
      return new Gson()
        .toJsonTree(new Gson().toJsonTree(componentPayload)
        );
    } catch (JsonParseException e) {
      response.status(400);
    }

    return "Error";
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
