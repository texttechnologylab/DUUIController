import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.io.xmi.XmiWriter;
import org.hibernate.annotations.common.reflection.XMember;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

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
                .withSkipVerification(true);
//                .withStorageBackend(new DUUIMongoStorageBackend(getConnectionURI()));

        composer.addDriver(new DUUIUIMADriver());
        composer.addDriver(new DUUIDockerDriver());

        composer.add(
                new DUUIUIMADriver.Component(
                        createEngineDescription(
                                BreakIteratorSegmenter.class
                        )
                )
        );
        composer.add(new DUUIDockerDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:latest"));

        JCas cas = JCasFactory.createText("Das ist ein Testsatz, den ich am 14.09.2023 geschrieben habe.", "de");
        composer.run(cas, "duui_spacy");

        for (Annotation annotation : cas.getAnnotationIndex()) {
            System.out.println(annotation.getType());
        }
        composer.shutdown();
    }
}
