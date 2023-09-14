import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;

public class Main {

  private static final String user = System.getenv("mongo_user");
  private static final String pass = System.getenv("mongo_pass");

  private static String getConnectionURI() {
    return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
        "<user>",
        user
      )
      .replace("<password>", pass);
  }

  public static void main(String[] args) throws Exception {
    DUUILuaContext context = new DUUILuaContext()
      .withGlobalLibrary(
        "json",
        DUUIComposer.class.getResourceAsStream("lua_stdlib/json.lua")
      );
    DUUIComposer composer = new DUUIComposer()
      .withLuaContext(context)
      .withSkipVerification(true)
      .withStorageBackend(new DUUIMongoStorageBackend(getConnectionURI()));

    composer.addDriver(new DUUIDockerDriver());
    composer.addDriver(new DUUIUIMADriver());

    composer.add(
      new DUUIUIMADriver.Component(
        AnalysisEngineFactory.createEngineDescription(
          "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter"
        )
      )
    );

    composer.add(
      new DUUIDockerDriver.Component(
        "docker.texttechnologylab.org/heideltime_ext:0.2"
      )
    );

    JCas cas = JCasFactory.createJCas();
    cas.setDocumentText("Das ist ein Testsatz.");
    composer.run(cas, "duui_spacy");

    for (Annotation annotation : cas.getAnnotationIndex()) {
      System.out.println(annotation.getClass().getCanonicalName());
    }
    composer.shutdown();
  }
}
