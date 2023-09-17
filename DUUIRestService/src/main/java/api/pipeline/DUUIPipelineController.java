package api.pipeline;

import static api.Application.queryIntElseDefault;
import static api.services.DUUIMongoService.mapObjectIdToString;

import api.Responses.MissingRequiredFieldResponse;
import api.Responses.NotFoundResponse;
import api.services.DUUIMongoService;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

public class DUUIPipelineController {

  public static String insertOne(Request request, Response response) {
    Document newPipeline = Document.parse(request.body());

    String name = newPipeline.getString("name");
    if (name == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("name").toJson();
    }

    Object components = newPipeline.get("components");
    if (components == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("components").toJson();
    }

    Document pipeline = new Document();

    pipeline.put("name", name);
    pipeline.put("components", components);
    pipeline.put("createdAt", new Date().toInstant().toEpochMilli());
    pipeline.put("isNew", true);

    response.status(200);
    DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("pipelines")
      .insertOne(pipeline);

    return new Document("id", pipeline.getObjectId("_id").toString()).toJson();
  }

  public static String findOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id").toJson();
    }

    Document pipeline = DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("pipelines")
      .find(Filters.eq(new ObjectId(id)))
      .first();

    if (pipeline == null) {
      response.status(404);
      return new NotFoundResponse("No pipeline found for id <" + id + ">")
        .toJson();
    }

    mapObjectIdToString(pipeline);
    response.status(200);
    return pipeline.toJson();
  }

  public static String findMany(Request request, Response response) {
    String userId = request.params(":user_id");
    if (userId == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("user_id").toJson();
    }

    
    int limit = queryIntElseDefault(request, "limit", 0);
    int offset = queryIntElseDefault(request, "offset", 0);

    FindIterable<Document> pipelines = DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("pipelines")
      .find(Filters.eq("user_id", new ObjectId(userId)));

      
    if (limit != 0) {
      pipelines.limit(limit);
    }

    if (offset != 0) {
      pipelines.skip(offset);
    }

    List<Document> documents = new ArrayList<>();
    pipelines.into(documents);

    documents.forEach(
      (
        document -> {
          mapObjectIdToString(document);
        }
      )
    );

    response.status(200);
    return new Document("pipelines", documents).toJson();
  }

  public static String replaceOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id").toJson();
    }

    Document updatedPipeline = Document.parse(request.body());

    DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("pipelines")
      .replaceOne(Filters.eq(new ObjectId(id)), updatedPipeline);

    response.status(200);
    return new Document("id", id).toJson();
  }

  public static String deleteOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id").toJson();
    }

    DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("pipelines")
      .deleteOne(Filters.eq(new ObjectId(id)));

    response.status(200);
    return new Document("message", "Pipeline deleted.").toJson();
  }
}
