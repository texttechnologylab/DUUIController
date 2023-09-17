package api;

import spark.Request;
import spark.Response;

public interface IDUUIController {
  public String findOne(Request request, Response response);

  public String findMany(Request request, Response response);

  public String insertOne(Request request, Response response);

  public String updateOne(Request request, Response response);

  public String deleteOne(Request request, Response response);
}
