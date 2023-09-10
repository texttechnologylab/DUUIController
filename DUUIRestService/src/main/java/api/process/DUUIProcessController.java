package api.process;

import api.services.DUUIMongoService;
import com.mongodb.client.model.Filters;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.print.Doc;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUISwarmDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.xml.sax.SAXException;
import spark.Request;
import spark.Response;

public class DUUIProcessController {

  private static HashMap<String, DUUIProcess> runningProcesses = new HashMap<>();

  public static String startProcess(Request request, Response response)
    throws UIMAException, URISyntaxException, IOException, InterruptedException, ExecutionException {
    Document processData = Document.parse(request.body());

    String pipelineId = processData.getString("id");
    if (pipelineId == null) {
      return new Document("Bad Request", "Missing required field pipeline_id")
        .toJson();
    }

    DUUILuaContext context = new DUUILuaContext()
      .withGlobalLibrary(
        "json",
        DUUIComposer.class.getResourceAsStream("lua_stdlib/json.lua")
      );

    Document pipeline = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq(new ObjectId(pipelineId)))
      .findOne();

    DUUIComposer composer = new DUUIComposer()
      .withLuaContext(context)
      .withSkipVerification(true);

    List<Document> components = pipeline.getList("components", Document.class);
    for (Document component : components) {
      String target = component.getString("target");
      System.out.println("---- " + target + " ----");
      try {
        switch (component.getString("driver")) {
          case "DUUIRemoteDriver" -> {
            composer.addDriver(new DUUIRemoteDriver(5000));
            composer.add(new DUUIRemoteDriver.Component(target));
          }
          case "DUUIUIMADriver" -> {
            composer.addDriver(new DUUIUIMADriver());
            composer.add(
              new DUUIUIMADriver.Component(
                AnalysisEngineFactory.createEngineDescription(target)
              )
            );
          }
          case "DUUIDockerDriver" -> {
            composer.addDriver(new DUUIDockerDriver().withTimeout(5000));
            composer.add(
              new DUUIDockerDriver.Component(target)
                .withGPU(true)
                .withImageFetching()
            );
          }
          case "DUUISwarmDriver" -> {
            composer.addDriver(new DUUISwarmDriver());
            composer.add(new DUUISwarmDriver.Component(target));
          }
          default -> {}
        }
      } catch (
        IOException
        | SAXException
        | CompressorException
        | UIMAException
        | URISyntaxException e
      ) {
        response.status(400);
        return new Document(
          "error",
          "Failed to instantiate component <" +
          component.getString("name") +
          ">."
        )
          .toJson();
      }
    }

    String uuid = UUID.randomUUID().toString();

    JCas cas;
    String documentText = processData.getString("document");
    if (documentText == null) {
      return new Document("error", "Empty document can not be annotated.")
        .toJson();
    }

    try {
      cas = JCasFactory.createJCas();
      cas.setDocumentText(documentText);
    } catch (UIMAException e) {
      response.status(400);
      return new Document("error", "JCas Object could not be instantiated.")
        .toJson();
    }

    CompletableFuture<Void> thread = CompletableFuture.runAsync(() -> {
      try {
        composer.run(cas, pipeline.getString("name") + "_" + new Date());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    DUUIProcess process = new DUUIProcess(uuid, composer, thread);
    runningProcesses.put(uuid, process);

    try {
      thread.get();
    } catch (CancellationException e) {
      System.out.println("Pipeline cancelled. Shutting down.");
    }

    if (thread.isCancelled()) {
      composer.interrupt();
    }

    composer.shutdown();
    runningProcesses.remove(uuid);
    return new Document("id", uuid).toJson();
  }

  public static String stopProcess(Request request, Response response) {
    Document processData = Document.parse(request.body());
    String processId = processData.getString("id");
    if (processId == null) {
      return new Document("Bad Request", "Missing required field id").toJson();
    }

    runningProcesses.get(processId).getThread().cancel(true);
    return new Document("id", processId).toJson();
  }
}
