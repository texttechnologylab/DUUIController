package api.component;

import static api.Application.queryIntElseDefault;
import static api.services.DUUIMongoService.mapObjectIdToString;

import api.Responses.DuplicateKeyResponse;
import api.Responses.MissingRequiredFieldResponse;
import api.Responses.NotFoundResponse;
import api.services.DUUIMongoService;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

public class DUUIComponentController {

  public static String insertOne(Request request, Response response) {
    Document newComponent = Document.parse(request.body());

    String name = newComponent.getString("name");
    if (name == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("name").toJson();
    }

    String driver = newComponent.getString("driver");
    if (driver == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("driver").toJson();
    }

    String target = newComponent.getString("target");
    if (target == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("driver").toJson();
    }

    String category = newComponent.getString("category");
    if (category == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("category").toJson();
    }

    Document options = newComponent.get("options", Document.class);
    String description = newComponent.getString("description");

    Document component = new Document();

    component.put("name", name);
    component.put("category", category);
    component.put("driver", driver);
    component.put("target", target);
    component.put("description", description);
    component.put("options", options);

    try {
        
        DUUIMongoService
        .getInstance()
        .getDatabase("duui")
        .getCollection("components")
        .insertOne(component);
    } catch (MongoWriteException e) {
        response.status(400);
        return new DuplicateKeyResponse("A Component with the name " + name + " already exists.").toJson();
    }

    response.status(200);
    return new Document("id", component.getObjectId("_id").toString()).toJson();
  }

  public static String findOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id").toJson();
    }

    Document component = DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("components")
      .find(Filters.eq(new ObjectId(id)))
      .first();

    if (component == null) {
      response.status(404);
      return new NotFoundResponse("No component found for id <" + id + ">")
        .toJson();
    }

    mapObjectIdToString(component);
    response.status(200);
    return component.toJson();
  }

  public static String findMany(Request request, Response response) {
    int limit = queryIntElseDefault(request, "limit", 0);
    int offset = queryIntElseDefault(request, "offset", 0);

    FindIterable<Document> components = DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("components")
      .find();

    if (limit != 0) {
      components.limit(limit);
    }

    if (offset != 0) {
      components.skip(offset);
    }

    List<Document> documents = new ArrayList<>();
    components.into(documents);

    documents.forEach(
      (
        document -> {
          mapObjectIdToString(document);
        }
      )
    );

    response.status(200);
    return new Document("components", documents).toJson();
  }

  public static String replaceOne(Request request, Response response) {
    String id = request.params(":id");
    if (id == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("id").toJson();
    }

    Document updatedComponents = Document.parse(request.body());

    DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("components")
      .replaceOne(Filters.eq(new ObjectId(id)), updatedComponents);

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
      .getCollection("components")
      .deleteOne(Filters.eq(new ObjectId(id)));

    response.status(200);
    return new Document("message", "Component deleted.").toJson();
  }
}
