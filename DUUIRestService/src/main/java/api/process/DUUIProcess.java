package api.process;

import api.Application;
import api.services.DUUIMongoService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.*;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.junit.jupiter.params.aggregator.ArgumentAccessException;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;
import org.xml.sax.SAXException;

public class DUUIProcess extends Thread {

  private final String id;
  private final Document pipeline;
  private final Document options;
  private DUUIComposer composer;
  private ScheduledExecutorService progressTrackerService = Executors.newScheduledThreadPool(
    1
  );
  private ScheduledFuture<?> progressTracker;

  public DUUIProcess(String id, Document pipeline, Document options) {
    this.id = id;
    this.pipeline = pipeline;
    this.options = options;
  }

  private IDUUIDriverInterface getDriverFromString(String driver)
    throws IOException, UIMAException, SAXException {
    return switch (driver) {
      case "DUUIRemoteDriver" -> new DUUIRemoteDriver(10000);
      case "DUUIDockerDriver" -> new DUUIDockerDriver(10000);
      case "DUUISwarmDriver" -> new DUUISwarmDriver(10000);
      case "DUUIUIMADriver" -> new DUUIUIMADriver();
      default -> null;
    };
  }

  private void setupDrivers() throws IOException, UIMAException, SAXException {
    for (Document component : pipeline.getList("components", Document.class)) {
      IDUUIDriverInterface driver = getDriverFromString(
        component.getString("driver")
      );
      if (driver == null) {
        throw new ArgumentAccessException("Driver cannot be empty.");
      }
      composer.addDriver(driver);
    }
  }

  private void setupComponents()
    throws IOException, UIMAException, SAXException, URISyntaxException, CompressorException {
    for (Document component : pipeline.getList("components", Document.class)) {
      switch (component.getString("driver")) {
        case "DUUIDockerDriver" -> composer.add(
          new DUUIDockerDriver.Component(component.getString("target"))
        );
        case "DUUISwarmDriver" -> composer.add(
          new DUUISwarmDriver.Component(component.getString("target"))
        );
        case "DUUIRemoteDriver" -> composer.add(
          new DUUIRemoteDriver.Component(component.getString("target"))
        );
        case "DUUIUIMADriver" -> composer.add(
          new DUUIUIMADriver.Component(
            AnalysisEngineFactory.createEngineDescription(
              component.getString("target")
            )
          )
        );
        default -> throw new IllegalArgumentException(
          "Driver cannot be empty."
        );
      }
    }
  }

  private void initializeComposerSingleDocument() throws Exception {
    composer =
      new DUUIComposer()
        .withSkipVerification(true)
        .withStorageBackend(
          new DUUIMongoStorageBackend(DUUIMongoService.getConnectionURI())
        )
        .withLuaContext(new DUUILuaContext().withJsonLibrary());

    setupDrivers();
    setupComponents();
  }

  @Override
  public void run() {
    try {
      initializeComposerSingleDocument();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    String document = options.getString("document");
    if (document == null) {
      throw new IllegalArgumentException("Document cannot be empty.");
    }

    JCas cas = null;
    try {
      cas = JCasFactory.createText(document);
    } catch (UIMAException e) {
      throw new RuntimeException(e);
    }

    progressTracker =
      progressTrackerService.scheduleAtFixedRate(
        () -> DUUIProcessController.setProgress(id, composer.getProgress()),
        0,
        2,
        TimeUnit.SECONDS
      );

    try {
      DUUIProcessController.setStatus(id, "running");
      Application.metrics.get("active_processes").incrementAndGet();
      composer.run(
        cas,
        pipeline.getString("name") + "_" + new Date().toInstant().toString()
      );
    } catch (Exception e) {
      DUUIProcessController.setStatus(id, "failed");
      Application.metrics.get("failed_processes").incrementAndGet();
      Application.metrics.get("active_processes").decrementAndGet();
      progressTracker.cancel(true);
      throw new RuntimeException(e);
    }

    try {
      composer.shutdown();
      DUUIProcessController.setStatus(id, "completed");
      progressTracker.cancel(true);
      Application.metrics.get("completed_processes").incrementAndGet();
      Application.metrics.get("active_processes").decrementAndGet();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  public void cancel() throws UnknownHostException {
    interrupt();
    composer.shutdown();
    DUUIProcessController.setStatus(id, "cancelled");
    progressTracker.cancel(true);
  }

  public DUUIComposer getComposer() {
    return composer;
  }

  public Document getPipeline() {
    return pipeline;
  }
}
