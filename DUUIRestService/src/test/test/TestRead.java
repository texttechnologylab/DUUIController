package test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIMinioDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.reader.DUUIDocumentReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class TestRead {

    private static String endpoint = "http://localhost:9000";
    private static String username = "minioadmin";
    private static String password = "minioadmin";

    @Test
    public void TestDocumentReader() throws Exception {
        DUUIComposer composer = new DUUIComposer()
            .withLuaContext(new DUUILuaContext().withJsonLibrary())
            .withSkipVerification(true)
            .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
            .withWorkers(5)
            .withIgnoreErrors(true);


        DUUIMinioDocumentHandler minio = new DUUIMinioDocumentHandler(
            endpoint,
            username,
            password
        );

        DUUIDocumentReader reader = DUUIDocumentReader
            .builder(composer)
            .withInputHandler(minio)
            .withInputPath("input/sample_txt")
            .withInputFileExtension(".txt")
            .withOutputHandler(minio)
            .withOutputPath("output/xmi")
            .withOutputFileExtension(".xmi")
            .withRecursive(true)
            .withSortBySize(true)
            .withCheckTarget(true)
            .withAddMetadata(true)
            .withMinimumDocumentSize(1024 * 3)
            .build();

        composer.addDriver(new DUUIUIMADriver());
        composer.addDriver(new DUUIDockerDriver());

        composer.add(new DUUIUIMADriver.Component(
            createEngineDescription(BreakIteratorSegmenter.class))
            .withName("Tokenizer"));

        composer.add(new DUUIDockerDriver.Component(
            "docker.texttechnologylab.org/gervader_duui:latest")
            .withName("GerVADER"));

        composer.run(reader, "example-minio");

        /**
         * Excerpt from the debug output of events in the process.
         * Timestamp     [SENDER]  : Message
         *
         * 1707413608359 [READER]  : Skip files smaller than 3072 bytes.
         * 1707413608359 [READER]  : Number of files before skipping 17.
         * 1707413608360 [READER]  : Number of files after skipping 13.
         * 1707413608361 [READER]  : Sorted files by size in ascending order
         * 1707413608361 [READER]  : Checking output location output/xmi for existing documents.
         * 1707413608372 [READER]  : Found 0 documents in output location. Keeping all files from input location.
         * 1707413608372 [READER]  : Processing 13 files.
         *
         * 1707413628184 [READER]  : Decoding document input/sample_txt/sample_14_92120.txt
         * 1707413628184 [READER]  : Document input/sample_txt/sample_14_92120.txt decoded after 0 ms
         * 1707413628184 [READER]  : Deserializing document input/sample_txt/sample_14_92120.txt
         * 1707413628190 [READER]  : Document input/sample_txt/sample_14_92120.txt deserialized after 6 ms
         * 1707413628190 [DOCUMENT]: Starting to process input/sample_txt/sample_14_92120.txt
         * 1707413628190 [DOCUMENT]: input/sample_txt/sample_14_92120.txt is being processed by component Tokenizer
         * 1707413628212 [DOCUMENT]: input/sample_txt/sample_14_92120.txt has been processed by component Tokenizer
         * 1707413628212 [DOCUMENT]: input/sample_txt/sample_14_92120.txt is being processed by component GerVADER
         * 1707413629297 [DOCUMENT]: input/sample_txt/sample_09_34261.txt has been processed by component GerVADER
         * 1707413629300 [READER]  : Uploading document input/sample_txt/sample_09_34261.txt
         * 1707413629357 [DOCUMENT]: input/sample_txt/sample_09_34261.txt has been processed after 3370 ms
         * 1707413629357 [COMPOSER]: 5 Documents have been processed
         */
    }

}
