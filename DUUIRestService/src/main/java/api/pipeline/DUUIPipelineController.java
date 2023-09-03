package api.pipeline;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import services.DUUIMongoService;
import spark.Request;
import spark.Response;

public class DUUIPipelineController {

  public static JSONObject findPipelineById(
    Request request,
    Response response
  ) {
    String id = request.queryParams("id");
    if (id == null) {
      response.status(400);
      return new JSONObject().put("message", "Missing required parameter id.");
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

  public static JSONArray findAllPipelines(
    Request request,
    Response response
  ) {
    String limitParameter = request.queryParams("limit");
    String offsetParameter = request.queryParams("offset");

    System.out.println(limitParameter + ", " + offsetParameter);

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

  public static void main(String[] args) {
    Logger logger = Logger.getLogger("org.mongodb.driver");
    logger.setLevel(Level.SEVERE);
    DUUIMongoService service = DUUIMongoService
      .PipelineService()
      .withFilter(Filters.eq("_id", new ObjectId("64f09376b7ff0557a37bb9ce")));
    System.out.println(service.findOne());
  }
}
