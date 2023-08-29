import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static spark.Spark.*;

import Storage.DUUISQLiteConnection;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.json.JSONArray;
import org.json.JSONObject;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUISwarmDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.xml.sax.SAXException;

import spark.Request;
import spark.Response;

public class DUUIRestService {

  private static HashMap<String, Tuple<CompletableFuture<Void>, DUUIComposer>> _threads = new HashMap<>();

  private class Tuple<X, Y> {

    public final X future;
    public final Y composer;

    public Tuple(X future, Y composer) {
      this.future = future;
      this.composer = composer;
    }
  }

  private static JSONObject parseBody(Request request) {
    return new JSONObject(request.body());
  }

  public static Object getPipelines(Request request, Response response) {
    if (request.queryParams("limit") == null) {
      return DUUISQLiteConnection.getPipelines();
    }

    if (request.queryParams("offset") == null) {
      return DUUISQLiteConnection.getPipelines(
        Integer.parseInt(request.queryParams("limit"))
      );
    }

    return DUUISQLiteConnection.getPipelines(
      Integer.parseInt(request.queryParams("limit")),
      Integer.parseInt(request.queryParams("offset"))
    );
  }

  public static Object getPipeline(Request request, Response response) {
    if (request.queryParams("id") == null) {
      return "No id provided.";
    }

    return DUUISQLiteConnection.getPipelineByID(request.queryParams("id"));
  }

  public static Object postPipeline(Request request, Response response) {
    JSONObject requestJSON = parseBody(request);

    if (!requestJSON.has("components")) {
      return "Pipeline has no components. Canceled insertion.";
    }

    if (requestJSON.getJSONArray("components").isEmpty()) {
      return "Pipeline has no components. Canceled insertion.";
    }

    if (requestJSON.has("name") && requestJSON.getString("name").isEmpty()) {
      requestJSON.put("name", "Unnamed");
    }

    DUUISQLiteConnection.insertPipeline(requestJSON);
    return "Inserted new Pipeline: " + requestJSON.getString("name");
  }

  public static Object putPipeline(Request request, Response response) {
    JSONObject requestJSON = parseBody(request);

    if (!requestJSON.has("id")) {
      return "Cannot update pipeline without id.";
    }

    if (!requestJSON.has("name")) {
      return "Empty name is not allowed";
    }

    if (
      !requestJSON.has("components") ||
      requestJSON.getJSONArray("components").isEmpty()
    ) {
      return "Pipeline without components is not allowed";
    }

    if (DUUISQLiteConnection.updatePipeline(requestJSON)) {
      return "Updated pipeline " + requestJSON.getString("id");
    }

    return "Something went wrong.";
  }

  public static Object runPipeline(Request request, Response response)
    throws Exception {
    String id = request.params(":id");
    if (id == null) {
      return "Invalid pipeline id";
    }

    JSONObject pipeline = DUUISQLiteConnection.getPipelineByID(id);
    if (pipeline == null) {
      return "Pipeline not found.";
    }

    response.type("application/xml");

    String name = pipeline.getString("name");
    JSONArray components = pipeline.getJSONArray("components");

    DUUIComposer composer = new DUUIComposer().withSkipVerification(true);

    DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
    DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver(5000);
    DUUIDockerDriver dockerDriver = new DUUIDockerDriver(5000);
    DUUISwarmDriver swarmDriver = new DUUISwarmDriver(5000);

    for (Object component : components) {
      String driver = ((JSONObject) component).getString("driver");
      String target = ((JSONObject) component).getString("target");

      switch (driver) {
        case "DUUIUIMADriver":
          composer.addDriver(uimaDriver);
          composer.add(
            new DUUIUIMADriver.Component(createEngineDescription(target))
          );
          break;
        case "DUUIRemoteDriver":
          composer.addDriver(remoteDriver);
          composer.add(new DUUIRemoteDriver.Component(target));
          break;
        case "DUUIDockerDriver":
          composer.addDriver(dockerDriver);
          composer.add(new DUUIDockerDriver.Component(target));
          break;
        case "DUUISwarmDriver":
          composer.addDriver(swarmDriver);
          composer.add(new DUUISwarmDriver.Component(target));
          break;
        default:
          break;
      }
    }

    JCas cas = JCasFactory.createJCas();
    cas.setDocumentText("This text is in english.");

    CompletableFuture<Void> thread = CompletableFuture.runAsync(() -> {
      composer.run(cas, name);
    });

    _threads.put(
      id,
      new Tuple<CompletableFuture<Void>, DUUIComposer>(thread, composer)
    );

    String result = null;
    thread.thenRun(() -> {
      try {
        composer.shutdown();
      } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      OutputStream xmiOutputStream = new ByteArrayOutputStream();
      try {
        XmiCasSerializer.serialize(cas.getCas(), xmiOutputStream);
      } catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      result = xmiOutputStream.toString();
    });

    return result;
  }

  public static Object getPipelineStatus(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      return "No pipeline id provided.";
    }

    return "";
  }

  public static void main(String[] args) {
    port(9090);

    options(
      "/*",
      (request, response) -> {
        String accessControlRequestHeaders = request.headers(
          "Access-Control-Request-Headers"
        );
        if (accessControlRequestHeaders != null) {
          response.header(
            "Access-Control-Allow-Headers",
            accessControlRequestHeaders
          );
        }

        String accessControlRequestMethod = request.headers(
          "Access-Control-Request-Method"
        );
        if (accessControlRequestMethod != null) {
          response.header(
            "Access-Control-Allow-Methods",
            accessControlRequestMethod
          );
        }

        return "OK";
      }
    );

    before((request, response) ->
      response.header("Access-Control-Allow-Origin", "*")
    );
    get("/", (request, response) -> "DUUI says hello");

    get("/pipeline", "application/json", DUUIRestService::getPipeline);
    post("/pipeline", "application/json", DUUIRestService::postPipeline);
    put("/pipeline", "application/json", DUUIRestService::putPipeline);
    get("/pipelines", "application/json", DUUIRestService::getPipelines);

    post(
      "/cancel/:id",
      "application/json",
      (request, response) -> {
        String id = request.params(":id");

        CompletableFuture<Void> task = _threads.get(id).future;
        DUUIComposer composer = _threads.get(id).composer;

        if (task == null) {
          response.status(404);
          return "[ERROR]: No such Task";
        }

        if (task.cancel(true)) {
          _threads.remove(id);
          composer.shutdown();
          response.status(200);
          task.complete(null);
          return "[OK]: Task has been canceled";
        }
        return "[ERROR]: Failed";
      }
    );

    get("/run/:id", "application/json", DUUIRestService::runPipeline);
    get("pipeline/status/:id", DUUIRestService::getPipelineStatus);
  }
}
