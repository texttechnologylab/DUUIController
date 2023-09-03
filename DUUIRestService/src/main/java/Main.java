// import static spark.Spark.*;

// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import models.DUUIPipeline;
// import org.json.JSONObject;

// import Storage.DUUISQLiteConnection;
// // import services.DUUIPipelineService;
// import spark.Request;
// import spark.Response;

// public class Main {

//   private static AtomicInteger activeRequests = new AtomicInteger(0);



//   private static String getPipelineStatus(Request request, Response response) {
//     return String.format(
//       "Pipeline with id <%s> has status <%s>",
//       request.params(":id"),
//       "Idle"
//     );
//   }

//   private static String addPipeline(Request request, Response response) {
//     DUUIPipelineService.insertPipeline(
//       new DUUIPipeline(new JSONObject(request.body()))
//     );
//     return String.format("Added pipeline with id.");
//   }

//   private static String getComponent(Request request, Response response) {
//     return String.format(
//       "Here is component with index <%s> of pipeline with id <%s>",
//       request.params(":index"),
//       request.params(":p_id")
//     );
//   }

//   private static String addComponent(Request request, Response response) {
//     return String.format(
//       "Added Component <%s> to pipeline with id <%s>",
//       request.body(),
//       request.params(":id")
//     );
//   }

//   private static String updatePipeline(Request request, Response response) {
//     if (
//       DUUIPipelineService.updatePipeline(
//         new DUUIPipeline(new JSONObject(request.body()))
//       )
//     ) {
//       return String.format("Updated pipeline <%s>", request.body());
//     }

//     return "[Error]: Cannot update pipeline.";
//   }

//   private static String setPipelineStatus(Request request, Response response) {
//     return String.format("Updated pipeline status to <%s>", request.body());
//   }

//   private static Object deletePipeline(Request request, Response response) {
//     DUUIPipelineService.deletePipeline(request.params(":id"));
//     return "Deleted";
//   }

//   public static void main(String[] args) {
//     Logger logger = Logger.getLogger("org.mongodb.driver");
//     logger.setLevel(Level.SEVERE);

//     port(2605);

//     options(
//       "/*",
//       (request, response) -> {
//         String accessControlRequestHeaders = request.headers(
//           "Access-Control-Request-Headers"
//         );
//         if (accessControlRequestHeaders != null) {
//           response.header(
//             "Access-Control-Allow-Headers",
//             accessControlRequestHeaders
//           );
//         }

//         String accessControlRequestMethod = request.headers(
//           "Access-Control-Request-Method"
//         );
//         if (accessControlRequestMethod != null) {
//           response.header(
//             "Access-Control-Allow-Methods",
//             accessControlRequestMethod
//           );
//         }
//         return "OK";
//       }
//     );

//     before((request, response) -> {
//       activeRequests.incrementAndGet();
//       response.header("Access-Control-Allow-Origin", "*");
//     });

//     after((request, response) -> {
//       activeRequests.decrementAndGet();
//     });

//     get("/", (request, response) -> "DUUI says hello");
//     get("/pipeline/:id", DUUIPipelineService::getPipelineById);
//     get("/pipeline", DUUIPipelineService::getPipelines);
//     get("pipeline/:p_id/component/:index", Main::getComponent);
//     get("pipeline/:id/status", DUUIPipelineService::getPipelineStatus);

//     post("/pipeline", Main::addPipeline);
//     post("/pipeline/:id/component", Main::addComponent);

//     put("/pipeline", Main::updatePipeline);
//     put("/pipeline/:id/status", Main::setPipelineStatus);

//     delete("/pipeline/:id", Main::deletePipeline);

//     get(
//       "/metrics",
//       (req, res) -> {
//         return "demo_api_http_requests_in_progress " + activeRequests.get();
//       }
//     );
//   }
// }
