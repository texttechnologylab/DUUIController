package api.pipeline;

import static api.services.DUUIMongoService.mapDateToString;
import static api.services.DUUIMongoService.mapObjectIdToString;

import api.services.DUUIMongoService;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
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

public class DUUIPipelineController {

  private static String queryStringElseDefault(
    Request request,
    String field,
    String fallback
  ) {
    return request.queryParams(field) == null
      ? fallback
      : request.queryParams(field);
  }

  private static int queryIntElseDefault(
    Request request,
    String field,
    int fallback
  ) {
    return request.queryParams(field) == null
      ? fallback
      : Integer.parseInt(request.queryParams(field));
  }

  public static String insertOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new Document("Bad Request", "Missing required field id").toJson();
    }
    response.status(200);
    DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq(new ObjectId(id)))
      .deleteOne();
    return new Document("Pipeline inserted", id).toJson();
  }

  public static String findOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new Document("Bad Request", "Missing required field id").toJson();
    }
    Document pipeline = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq(new ObjectId(id)))
      .findOne();

    mapDateToString(pipeline, "createdAt");
    mapObjectIdToString(pipeline);

    response.status(200);

    return pipeline.toJson();
  }

  public static String findMany(Request request, Response response) {
    int limit = queryIntElseDefault(request, "limit", 0);
    int offset = queryIntElseDefault(request, "offset", 0);

    DUUIMongoService service = DUUIMongoService.PipelineService();

    AggregateIterable<Document> pipelines;

    if (limit > 0) {
      pipelines = service.findMany(limit, offset);
    } else {
      pipelines = service.findMany();
    }

    Document output = new Document();
    List<Document> documents = new ArrayList<>();

    pipelines.into(documents);

    documents.forEach(
      (
        document -> {
          mapDateToString(document, "createdAt");
          mapObjectIdToString(document);
        }
      )
    );

    output.put("pipelines", documents);
    response.status(200);
    return output.toJson();
  }

  public static String updateOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new Document("Bad Request", "Missing required field id").toJson();
    }

    Document updates = Document.parse(request.body());
    DUUIMongoService service = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq(new ObjectId(id)));
    service.updateOne(updates.toBsonDocument());
    response.status(200);
    return new Document("Success", "Pipeline updated").toJson();
  }

  public static String deleteOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new Document("Bad Request", "Missing required field id").toJson();
    }
    response.status(200);
    DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq(new ObjectId(id)))
      .deleteOne();
    return new Document("Success", "Pipeline deleted").toJson();
  }

  public static JSONObject findPipelineById(
    Request request,
    Response response
  ) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id");
    }
    DUUIMongoService mongoService = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq("_id", new ObjectId(id)));

    Document document = mongoService.findOne();
    response.status(200);

    if (document == null) {
      return new JSONObject();
    }

    JSONObject pipeline = new JSONObject(document.toJson());
    pipeline.remove("_id");
    return pipeline;
  }

  public static JSONObject deletePipeline(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new JSONObject().put("message", "Missing required parameter id.");
    }

    response.status(200);

    DUUIMongoService mongoService = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq("_id", new ObjectId(id)));

    String removedId = mongoService.deleteOne();
    return new JSONObject().put("id", removedId);
  }

  public static JSONArray findAllPipelines(Request request, Response response) {
    String limitParameter = request.queryParams("limit");
    String offsetParameter = request.queryParams("offset");

    int limit = limitParameter == null ? 0 : Integer.parseInt(limitParameter);
    int offset = offsetParameter == null
      ? 0
      : Integer.parseInt(offsetParameter);

    DUUIMongoService mongoService = DUUIMongoService.PipelineService();
    AggregateIterable<Document> pipelines;

    if (limit == 0) {
      pipelines = mongoService.findAll();
    } else {
      pipelines = mongoService.findAll(limit, offset);
    }

    JSONArray pipelinesArray = new JSONArray();
    response.status(200);

    pipelines.forEach(pipelineDocument -> {
      JSONObject pipelineJSON = new JSONObject(pipelineDocument.toJson());
      pipelineJSON.remove("_id");
      pipelinesArray.put(pipelineJSON);
    });

    return pipelinesArray;
  }

  public static JSONObject insertPipeline(Request request, Response response) {
    JSONObject requestPipeline = new JSONObject(request.body());
    DUUIMongoService service = DUUIMongoService.PipelineService();
    Document pipeline;

    try {
      List<Document> components = new ArrayList<>();
      requestPipeline
        .getJSONArray("components")
        .forEach(component -> {
          components.add(Document.parse(component.toString()));
        });

      pipeline =
        new Document("name", requestPipeline.getString("name"))
          .append("status", "New")
          .append("createdAt", new Date())
          .append("components", components);
    } catch (JSONException e) {
      response.status(400);
      return new JSONObject()
        .put("message", "error")
        .put("error", e.getMessage());
    }

    boolean success = service.insertOne(pipeline);
    String oid = pipeline.getObjectId("_id").toString();

    if (success) {
      response.status(200);
      return new JSONObject().put("message", "success").put("id", oid);
    }
    response.status(400);
    return new JSONObject().put("message", "error");
  }

  public static JSONObject updatePipeline(Request request, Response response) {
    JSONObject resquestJSON = new JSONObject(request.body());
    String id;
    try {
      id = resquestJSON.getString("id");
    } catch (JSONException e) {
      response.status(400);
      return new JSONObject()
        .put("message", "error")
        .put("error", "Missing required field id");
    }

    if (id == null) {
      response.status(400);
      return new JSONObject().put("message", "Missing required parameter id.");
    }

    DUUIMongoService service = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq("_id", new ObjectId(id)));
    service.replaceOne(id, resquestJSON);

    return new JSONObject().put("message", "success");
  }

  public static JSONObject startPipeline(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id");
    }

    DUUIMongoService service = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq("_id", new ObjectId(id)))
      .withProjection(
        Projections.fields(Projections.include("name", "components"))
      );

    Document document = service.findOne();
    JSONObject pipeline = new JSONObject(document.toJson());
    pipeline.remove("_id");

    System.out.println("---------------------------------------");
    System.out.println(pipeline);
    System.out.println("---------------------------------------");

    DUUIComposer composer;
    DUUILuaContext context;
    try {
      context =
        new DUUILuaContext()
          .withGlobalLibrary(
            "json",
            DUUIComposer.class.getResourceAsStream("lua_stdlib/json.lua")
          );
    } catch (IOException e) {
      response.status(500);
      throw new RuntimeException(e);
    }

    try {
      composer =
        new DUUIComposer().withLuaContext(context).withSkipVerification(true);
    } catch (URISyntaxException e) {
      response.status(400);
      return new JSONObject("error", "Composer could not be instantiated.");
    }

    JSONArray components = pipeline.getJSONArray("components");

    for (Object component : components) {
      JSONObject componentObject = (JSONObject) component;
      String target = componentObject.getString("target");
      System.out.println("---- " + target + " ----");
      try {
        switch (componentObject.getString("driver")) {
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
        return new JSONObject()
          .put(
            "error",
            "Failed to instantiate component <" +
            componentObject.getString("name") +
            ">."
          );
      }
    }

    JCas cas;
    Document doc = Document.parse(request.body());
    String documentText = doc.getString("doc");
    try {
      cas = JCasFactory.createJCas();
      cas.setDocumentText(documentText);
    } catch (UIMAException e) {
      response.status(400);
      return new JSONObject().put("error", "Cas could not be instantiated.");
    }

    Date now = new Date();
    CompletableFuture<JSONObject> task = CompletableFuture.supplyAsync(() -> {
      try {
        composer.run(cas, pipeline.getString("name") + "_" + now);
      } catch (InterruptedException e) {
        response.status(400);
        return new JSONObject()
          .put("message", "Pipeline has been canceled.")
          .put("error", e.getMessage());
      } catch (Exception e) {
        response.status(400);
        return new JSONObject()
          .put("message", "Failed to start pipeline.")
          .put("error", e.getMessage());
      }

      response.status(200);
      response.type("application/json");
      return null;
    });

    DUUIMongoService runService = DUUIMongoService.ActivityService();
    Document run = new Document("pipelineId", id)
      .append("startedAt", new Date())
      .append("status", "Running")
      .append("documentText", cas.getDocumentText());

    runService.insertOne(run);
    service.updatePipelineStatus(id, "Running");

    try {
      task.get();
      response.status(200);
      composer.shutdown();

      service.updatePipelineStatus(id, "Completed");
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      XmiCasSerializer.serialize(cas.getCas(), result);

      HashMap<String, Annotation> annotations = new HashMap<>();
      for (Annotation annotation : cas.getAnnotationIndex()) {
        annotations.put(annotation.getClass().getCanonicalName(), annotation);
      }

      JSONObject counter = new JSONObject();
      for (Annotation annotation : annotations.values()) {
        if (
          Objects.equals(
            annotation.getClass().getCanonicalName(),
            "org.apache.uima.jcas.tcas.DocumentAnnotation"
          )
        ) {
          continue;
        }
        counter.put(
          annotation.getClass().getCanonicalName(),
          JCasUtil.select(cas, annotation.getClass()).size()
        );
        System.out.println(annotation.getClass().getName());
        System.out.println(counter.get(annotation.getClass().getName()));
      }

      //            service.updateOne(id,
      //                    Updates.set("result", result.toString(StandardCharsets.UTF_8))
      //            );
      return counter;
    } catch (
      InterruptedException | ExecutionException | UnknownHostException e
    ) {
      response.status(400);
      try {
        composer.shutdown();
      } catch (UnknownHostException e1) {}
      service.updatePipelineStatus(id, "Error");

      return new JSONObject()
        .put("message", "Pipeline failed.")
        .put("error", e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  public static JSONObject stopPipeline(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id");
    }

    DUUIMongoService service = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq("_id", new ObjectId(id)));

    service.updatePipelineStatus(id, "Cancelled");
    return new JSONObject().put("message", "Cancelled pipeline");
  }

  public static JSONObject getPipelineStatus(
    Request request,
    Response response
  ) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id");
    }

    DUUIMongoService service = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq("_id", new ObjectId(id)))
      .withProjection(Projections.fields(Projections.include("status")));

    Document pipeline = service.findOne();
    response.status(200);
    return new JSONObject().put("id", id).put("status", pipeline.get("status"));
  }

  public static JSONObject getResult(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id");
    }
    DUUIMongoService service = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq(new ObjectId(id)));
    Document document = service.getRecentRun();
    return new JSONObject().put("result", document.getString("result"));
  }
}
