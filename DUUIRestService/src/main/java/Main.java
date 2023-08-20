import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.xmi.XmiWriter;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUISwarmDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaSandbox;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.LuaConsts;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.sqlite.DUUISqliteStorageBackend;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Main {

    public static void main(String[] args) throws Exception {
        int iWorkers = 2;

        JCas jc = JCasFactory.createJCas();
        DUUILuaContext context = new DUUILuaContext();

        context.withSandbox(new DUUILuaSandbox()
                .withAllowedJavaClass("java.lang.String")
                .withAllowedJavaClass("java.nio.charset.StandardCharsets")
                .withAllowedJavaClass("org.apache.uima.fit.util.JCasUtil")
                .withAllowedJavaClass("Taxon"));

        DUUISqliteStorageBackend sqlite = new DUUISqliteStorageBackend("loggingSQlite.db")
                .withConnectionPoolSize(iWorkers);

        DUUIComposer composer = new DUUIComposer()
                .withLuaContext(context)
                .withWorkers(iWorkers)
                .withStorageBackend(sqlite)
                .withSkipVerification(true);

        DUUIDockerDriver docker_driver = new DUUIDockerDriver().withTimeout(10000);
        DUUIRemoteDriver remote_driver = new DUUIRemoteDriver(10000);
        DUUIUIMADriver uima_driver = new DUUIUIMADriver().withDebug(true);
        DUUISwarmDriver swarm_driver = new DUUISwarmDriver();

        composer.addDriver(docker_driver, remote_driver, uima_driver, swarm_driver);
        composer.add(new DUUIDockerDriver.Component("gnfinder:latest").withScale(iWorkers).withImageFetching());
        composer.add(new DUUIUIMADriver.Component(createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, "out")).withScale(iWorkers));

        composer.run(jc, "Test");
    }
}
