package api.process;

import api.services.DUUIMongoService;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
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
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.LuaConsts;
import org.xml.sax.SAXException;
import spark.Request;
import spark.Response;

import static api.Application.queryIntElseDefault;
import static api.services.DUUIMongoService.mapDateToString;
import static api.services.DUUIMongoService.mapObjectIdToString;

public class DUUIProcessController {

    private static HashMap<String, DUUIProcess> runningProcesses = new HashMap<>();

    public static String startProcess(Request request, Response response)
            throws UIMAException, URISyntaxException, IOException, InterruptedException, ExecutionException {

        String id = request.params(":id");
        if (id == null) {
            return new Document("Bad Request", "Missing required parameter id.").toJson();
        }

        Document options = Document.parse(request.body());
        Document pipeline = DUUIMongoService.PipelineService()
                .withIdFilter(id)
                .findOne();

        String processName = pipeline.getString("name") + "_" + new Date().toInstant().toEpochMilli();
        DUUIComposer composer = new DUUIComposer();
        composer.withSkipVerification(true);

        composer.withLuaContext(LuaConsts.getJSON());

        return id;


//
//    DUUILuaContext context = new DUUILuaContext()
//      .withGlobalLibrary(
//        "json",
//        DUUIComposer.class.getResourceAsStream("lua_stdlib/json.lua")
//      );
//
//    Document pipeline = DUUIMongoService
//      .PipelineService()
//      .withFilter(Filters.eq(new ObjectId(pipelineId)))
//      .findOne();
//
//    DUUIComposer composer = new DUUIComposer()
//      .withLuaContext(context)
//      .withSkipVerification(true);
//
//    List<Document> components = pipeline.getList("components", Document.class);
//    for (Document component : components) {
//      String target = component.getString("target");
//      System.out.println("---- " + target + " ----");
//      try {
//        switch (component.getString("driver")) {
//          case "DUUIRemoteDriver" -> {
//            composer.addDriver(new DUUIRemoteDriver(5000));
//            composer.add(new DUUIRemoteDriver.Component(target));
//          }
//          case "DUUIUIMADriver" -> {
//            composer.addDriver(new DUUIUIMADriver());
//            composer.add(
//              new DUUIUIMADriver.Component(
//                AnalysisEngineFactory.createEngineDescription(target)
//              )
//            );
//          }
//          case "DUUIDockerDriver" -> {
//            composer.addDriver(new DUUIDockerDriver().withTimeout(5000));
//            composer.add(
//              new DUUIDockerDriver.Component(target)
//                .withGPU(true)
//                .withImageFetching()
//            );
//          }
//          case "DUUISwarmDriver" -> {
//            composer.addDriver(new DUUISwarmDriver());
//            composer.add(new DUUISwarmDriver.Component(target));
//          }
//          default -> {}
//        }
//      } catch (
//        IOException
//        | SAXException
//        | CompressorException
//        | UIMAException
//        | URISyntaxException e
//      ) {
//        response.status(400);
//        return new Document(
//          "error",
//          "Failed to instantiate component <" +
//          component.getString("name") +
//          ">."
//        )
//          .toJson();
//      }
//    }
//
//    String uuid = UUID.randomUUID().toString();
//
//    JCas cas;
//    String documentText = processData.getString("document");
//    if (documentText == null) {
//      return new Document("error", "Empty document can not be annotated.")
//        .toJson();
//    }
//
//    try {
//      cas = JCasFactory.createJCas();
//      cas.setDocumentText(documentText);
//    } catch (UIMAException e) {
//      response.status(400);
//      return new Document("error", "JCas Object could not be instantiated.")
//        .toJson();
//    }
//
//    CompletableFuture<Void> thread = CompletableFuture.runAsync(() -> {
//      try {
//        composer.run(cas, pipeline.getString("name") + "_" + new Date());
//      } catch (Exception e) {
//        throw new RuntimeException(e);
//      }
//    });
//
//    DUUIProcess process = new DUUIProcess(uuid, composer, thread);
//    runningProcesses.put(uuid, process);
//
//    try {
//      thread.get();
//    } catch (CancellationException e) {
//      System.out.println("Pipeline cancelled. Shutting down.");
//    }
//
//    if (thread.isCancelled()) {
//      composer.shutdown();
//    }
//
//    runningProcesses.remove(uuid);
//    return new Document("id", uuid).toJson();
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

    public static String findOne(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new Document("Bad Request", "Missing required field id").toJson();
        }
        Document process = DUUIMongoService
                .ProcessService()
                .withFilter(Filters.eq(new ObjectId(id)))
                .findOne();

        mapDateToString(process, "createdAt");
        mapObjectIdToString(process);

        response.status(200);

        return process.toJson();
    }

    public static Object findMany(Request request, Response response) {
        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        DUUIMongoService service = DUUIMongoService.ProcessService();

        AggregateIterable<Document> processes;

        if (limit > 0) {
            processes = service.findMany(limit, offset);
        } else {
            processes = service.findMany();
        }

        Document output = new Document();
        List<Document> documents = new ArrayList<>();

        processes.into(documents);

        documents.forEach(
                (
                        document -> {
                            mapDateToString(document, "createdAt");
                            mapObjectIdToString(document);
                        }
                )
        );

        output.put("processes", documents);
        response.status(200);
        return output.toJson();
    }
}
