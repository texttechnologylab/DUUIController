package api.users;

import static api.Application.queryIntElseDefault;
import static api.services.DUUIMongoService.mapObjectIdToString;

import api.Responses.MissingRequiredFieldResponse;
import api.services.DUUIMongoService;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

public class DUUIUserController {

  public static String findOneById(Request request, Response response) {
    String email = request.params(":email");
    if (email == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("email").toJson();
    }

    Document user = DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .find(Filters.eq("email", email))
            .first();

    if (user == null) {
      response.status(404);
      return new Document().toJson();
    }

    mapObjectIdToString(user);
    response.status(200);
    return user.toJson();
  }
  public static String findOneByEmail(Request request, Response response) {
    String email = request.params(":email");
    if (email == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("email").toJson();
    }

    Document user = DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("users")
      .find(Filters.eq("email", email))
      .first();

    if (user == null) {
      response.status(404);
      return new Document().toJson();
    }

    mapObjectIdToString(user);
    response.status(200);
    return user.toJson();
  }

  public static String findOneByToken(Request request, Response response) {
    String token = request.params(":token");
    if (token == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("token").toJson();
    }

    Document user = DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("users")
      .find(Filters.eq("userAuthToken", token))
      .projection(Projections.include("email", "userAuthToken", "role"))
      .first();

    if (user == null) {
      response.status(404);
      return new Document().toJson();
    }
    mapObjectIdToString(user);
    response.status(200);
    return user.toJson();
  }

  public static String findMany(Request request, Response response) {
    int limit = queryIntElseDefault(request, "limit", 0);
    int offset = queryIntElseDefault(request, "offset", 0);

    FindIterable<Document> users = DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("users")
      .find();

    if (limit != 0) {
      users.limit(limit);
    }

    if (offset != 0) {
      users.skip(offset);
    }

    List<Document> documents = new ArrayList<>();
    users.into(documents);

    documents.forEach(
      (
        document -> {
          mapObjectIdToString(document);
        }
      )
    );

    response.status(200);
    return new Document("users", documents).toJson();
  }

  public static String insertOne(Request request, Response response) {
    Document newUser = Document.parse(request.body());

    String email = newUser.getString("email");
    if (email == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("email").toJson();
    }

    String userAuthToken = newUser.getString("userAuthToken");
    if (userAuthToken == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("userAuthToken").toJson();
    }

    String role = newUser.getString("role");
    if (role == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("role").toJson();
    }

    String password = newUser.getString("password");
    if (password == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("password").toJson();
    }

    Document user = new Document();

    user.put("email", email);
    user.put("password", password);
    user.put("createdAt", new Date().toInstant().toEpochMilli());
    user.put("userAuthToken", userAuthToken);
    user.put("role", role);

    response.status(200);
    DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("users")
      .insertOne(user);

    return new Document("id", user.getObjectId("_id").toString()).toJson();
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
      .getCollection("users")
      .deleteOne(Filters.eq(new ObjectId(id)));

    response.status(200);
    return new Document("message", "User deleted.").toJson();
  }

  public static String updateOne(Request request, Response response) {
    Document newUser = Document.parse(request.body());

    String email = newUser.getString("email");
    if (email == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("email").toJson();
    }

    String userAuthToken = newUser.getString("userAuthToken");
    if (userAuthToken == null) {
      response.status(400);
      return new MissingRequiredFieldResponse("userAuthToken").toJson();
    }

    response.status(200);
    DUUIMongoService
      .getInstance()
      .getDatabase("duui")
      .getCollection("users")
      .findOneAndUpdate(
        Filters.eq("email", email),
        Updates.set("userAuthToken", userAuthToken)
      );

    return new Document("userAuthToken", userAuthToken).toJson();
  }
}
