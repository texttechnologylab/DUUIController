package api.process;

import static api.Application.queryIntElseDefault;
import static api.services.DUUIMongoService.mapObjectIdToString;

import api.Application;
import api.Responses.MissingRequiredFieldResponse;
import api.Responses.NotFoundResponse;
import api.services.DUUIMongoService;
import api.services.DUUIRequestValidator;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

import java.net.UnknownHostException;
import java.util.*;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.javatuples.Pair;
import spark.Request;
import spark.Response;

public class DUUIProcessController {

    private static final Map<String, DUUIProcess> runningProcesses = new HashMap<>();

    public static String startProcess(Request request, Response response) {
        Pair<Boolean, Object> validator = DUUIRequestValidator.validateBody(
                request,
                "pipeline_id"
        );

        if (!validator.getValue0()) {
            response.status(400);
            return new MissingRequiredFieldResponse("pipeline_id").toJson();
        }

        Document body = (Document) validator.getValue1();
        String pipelineId = body.getString("pipeline_id");
        Document options = body.get("options", Document.class);

        Document pipeline = DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("pipelines")
                .find(Filters.eq(new ObjectId(pipelineId)))
                .first();

        if (pipeline == null) {
            return new NotFoundResponse(
                    "No pipeline with id <" +
                            pipelineId +
                            "> found. Couldn't start process."
            )
                    .toJson();
        }

        Document process = new Document("status", "setup")
                .append("pipeline_id", pipelineId)
                .append("progress", 0)
                .append("startedAt", new Date().toInstant().toEpochMilli())
                .append("options", options);

        DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .insertOne(process);

        DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("pipelines")
                .updateOne(
                        Filters.eq(new ObjectId(pipelineId)),
                        Updates.set("isNew", false)
                );


        String id = process.getObjectId("_id").toString();
        runningProcesses.put(id, new DUUIProcess(id, pipeline, options));
        try {
            runningProcesses.get(id).start();
            return process.toJson();
        } catch (IllegalArgumentException e) {
            return new MissingRequiredFieldResponse("options/document").toJson();
        }
    }

    public static String stopProcess(Request request, Response response)
            throws UnknownHostException {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .find(
                        Filters.and(
                                Filters.eq(new ObjectId(id)),
                                Filters.eq("status", "running")
                        )
                )
                .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                    .toJson();
        }

        DUUIProcess p = runningProcesses.get(id);
        p.cancel();
        runningProcesses.remove(id);
        Application.metrics.get("active_processes").decrementAndGet();
        Application.metrics.get("cancelled_processes").incrementAndGet();

        return new Document("id", id).toJson();
    }

    public static String findOne(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .find(Filters.eq(new ObjectId(id)))
                .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                    .toJson();
        }

        mapObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static String findMany(Request request, Response response) {
        String pipelineId = request.params(":id");
        if (pipelineId == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        FindIterable<Document> pipelines = DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .find(Filters.eq("pipeline_id", pipelineId));

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
        return new Document("processes", documents).toJson();
    }

    public static String getStatus(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .find(Filters.eq(new ObjectId(id)))
                .projection(Projections.fields(Projections.include("status")))
                .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                    .toJson();
        }

        mapObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static String getProgress(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .find(Filters.eq(new ObjectId(id)))
                .projection(Projections.fields(Projections.include("progress")))
                .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                    .toJson();
        }

        mapObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static void setStatus(String id, String status) {
        DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .updateOne(Filters.eq(new ObjectId(id)), Updates.set("status", status));
    }

    public static void setProgress(String id, int progress) {
        DUUIMongoService
                .getInstance()
                .getDatabase("duui")
                .getCollection("processes")
                .updateOne(
                        Filters.eq(new ObjectId(id)),
                        Updates.set("progress", progress)
                );
    }
    //   public static String startProcess(Request request, Response response)
    //     throws UIMAException, URISyntaxException, IOException, InterruptedException, ExecutionException {
    //     Application.metrics.get("active_processes").incrementAndGet();

    //     String id = request.params(":id");
    //     if (id == null) {
    //       return new Document("Bad Request", "Missing required parameter id.")
    //         .toJson();
    //     }

    //     Document options = Document.parse(request.body());
    //     Document pipeline = DUUIMongoService
    //       .PipelineService()
    //       .withIdFilter(id)
    //       .findOne();

    //     String processName =
    //       pipeline.getString("name") + "_" + new Date().toInstant().toEpochMilli();
    //     DUUIComposer composer = new DUUIComposer();
    //     composer.withSkipVerification(true);

    //     composer.withLuaContext(LuaConsts.getJSON());

    //     return id;
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
    //   }

    // public static String startPipeline(Request request, Response response) {
    //   String id = request.params(":id");
    //   if (id == null) {
    //     response.status(400);
    //     return new MissingRequiredFieldResponse("id").toJson();
    //   }

    //   DUUIMongoService service = DUUIMongoService
    //     .PipelineService()
    //     .withFilter(Filters.eq("_id", new ObjectId(id)))
    //     .withProjection(
    //       Projections.fields(Projections.include("name", "components"))
    //     );

    //   Document document = service.findOne();
    //   JSONObject pipeline = new JSONObject(document.toJson());
    //   pipeline.remove("_id");

    //   System.out.println("---------------------------------------");
    //   System.out.println(pipeline);
    //   System.out.println("---------------------------------------");

    //   DUUIComposer composer;
    //   DUUILuaContext context;
    //   try {
    //     context =
    //       new DUUILuaContext()
    //         .withGlobalLibrary(
    //           "json",
    //           DUUIComposer.class.getResourceAsStream("lua_stdlib/json.lua")
    //         );
    //   } catch (IOException e) {
    //     response.status(500);
    //     throw new RuntimeException(e);
    //   }

    //   try {
    //     composer =
    //       new DUUIComposer().withLuaContext(context).withSkipVerification(true);
    //   } catch (URISyntaxException e) {
    //     response.status(400);
    //     return new Document("error", "Composer could not be instantiated.")
    //       .toJson();
    //   }

    //   JSONArray components = pipeline.getJSONArray("components");

    //   for (Object component : components) {
    //     JSONObject componentObject = (JSONObject) component;
    //     String target = componentObject.getString("target");
    //     System.out.println("---- " + target + " ----");
    //     try {
    //       switch (componentObject.getString("driver")) {
    //         case "DUUIRemoteDriver" -> {
    //           composer.addDriver(new DUUIRemoteDriver(5000));
    //           composer.add(new DUUIRemoteDriver.Component(target));
    //         }
    //         case "DUUIUIMADriver" -> {
    //           composer.addDriver(new DUUIUIMADriver());
    //           composer.add(
    //             new DUUIUIMADriver.Component(
    //               AnalysisEngineFactory.createEngineDescription(target)
    //             )
    //           );
    //         }
    //         case "DUUIDockerDriver" -> {
    //           composer.addDriver(new DUUIDockerDriver().withTimeout(5000));
    //           composer.add(
    //             new DUUIDockerDriver.Component(target)
    //               .withGPU(true)
    //               .withImageFetching()
    //           );
    //         }
    //         case "DUUISwarmDriver" -> {
    //           composer.addDriver(new DUUISwarmDriver());
    //           composer.add(new DUUISwarmDriver.Component(target));
    //         }
    //         default -> {}
    //       }
    //     } catch (
    //       IOException
    //       | SAXException
    //       | CompressorException
    //       | UIMAException
    //       | URISyntaxException e
    //     ) {
    //       response.status(400);
    //       return new Document(
    //         "error",
    //         "Failed to instantiate component <" +
    //         componentObject.getString("name") +
    //         ">."
    //       )
    //         .toJson();
    //     }
    //   }

    //   JCas cas;
    //   Document doc = Document.parse(request.body());
    //   String documentText = doc.getString("doc");
    //   try {
    //     cas = JCasFactory.createJCas();
    //     cas.setDocumentText(documentText);
    //   } catch (UIMAException e) {
    //     response.status(400);
    //     return new Document("error", "Cas could not be instantiated.").toJson();
    //   }

    //   Date now = new Date();
    //   CompletableFuture<JSONObject> task = CompletableFuture.supplyAsync(() -> {
    //     try {
    //       composer.run(cas, pipeline.getString("name") + "_" + now);
    //     } catch (InterruptedException e) {
    //       response.status(400);
    //       return new JSONObject()
    //         .put("message", "Pipeline has been canceled.")
    //         .put("error", e.getMessage());
    //     } catch (Exception e) {
    //       response.status(400);
    //       return new JSONObject()
    //         .put("message", "Failed to start pipeline.")
    //         .put("error", e.getMessage());
    //     }

    //     response.status(200);
    //     response.type("application/json");
    //     return null;
    //   });

    //   DUUIMongoService runService = DUUIMongoService.ProcessService();
    //   Document run = new Document("pipelineId", id)
    //     .append("startedAt", new Date())
    //     .append("status", "Running")
    //     .append("documentText", cas.getDocumentText());

    //   runService.insertOne(run);
    //   service.updatePipelineStatus(id, "Running");

    //   try {
    //     task.get();
    //     response.status(200);
    //     composer.shutdown();

    //     service.updatePipelineStatus(id, "Completed");
    //     ByteArrayOutputStream result = new ByteArrayOutputStream();
    //     XmiCasSerializer.serialize(cas.getCas(), result);

    //     HashMap<String, Annotation> annotations = new HashMap<>();
    //     for (Annotation annotation : cas.getAnnotationIndex()) {
    //       annotations.put(annotation.getClass().getCanonicalName(), annotation);
    //     }

    //     Document counter = new Document();
    //     for (Annotation annotation : annotations.values()) {
    //       if (
    //         Objects.equals(
    //           annotation.getClass().getCanonicalName(),
    //           "org.apache.uima.jcas.tcas.DocumentAnnotation"
    //         )
    //       ) {
    //         continue;
    //       }
    //       counter.put(
    //         annotation.getClass().getCanonicalName(),
    //         JCasUtil.select(cas, annotation.getClass()).size()
    //       );
    //       System.out.println(annotation.getClass().getName());
    //       System.out.println(counter.get(annotation.getClass().getName()));
    //     }

    //     //            service.updateOne(id,
    //     //                    Updates.set("result", result.toString(StandardCharsets.UTF_8))
    //     //            );
    //     return counter.toJson();
    //   } catch (
    //     InterruptedException | ExecutionException | UnknownHostException e
    //   ) {
    //     response.status(400);
    //     try {
    //       composer.shutdown();
    //     } catch (UnknownHostException e1) {}
    //     service.updatePipelineStatus(id, "Error");

    //     return new Document("message", "Pipeline failed.")
    //       .append("error", e.getMessage())
    //       .toJson();
    //   } catch (SAXException e) {
    //     throw new RuntimeException(e);
    //   }
    // }

    // public static String stopPipeline(Request request, Response response) {
    //   String id = request.params(":id");
    //   if (id == null) {
    //     response.status(400);
    //     return new MissingRequiredFieldResponse("id").toJson();
    //   }

    //   DUUIMongoService service = DUUIMongoService
    //     .PipelineService()
    //     .withFilter(Filters.eq("_id", new ObjectId(id)));

    //   service.updatePipelineStatus(id, "Cancelled");
    //   return new Document("message", "Cancelled pipeline").toJson();
    // }

}
