import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.dkpro.core.io.xmi.XmiWriter;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;


public class Main {
    public static void main(String[] args) throws Exception {


        DUUILuaContext context = new DUUILuaContext()
                .withGlobalLibrary("json", DUUIComposer.class.getResourceAsStream("lua_stdlib/json.lua"));
        DUUIComposer composer = new DUUIComposer().withLuaContext(context).withSkipVerification(true).withStorageBackend(
                new DUUIMongoStorageBackend("mongodb+srv://<pass>:<user>@testcluster.727ylpr.mongodb.net/")
        ).withWorkers(5);
        composer.addDriver(new DUUIUIMADriver());
        composer.add(
                new DUUIUIMADriver.Component(AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class))
        );
        composer.add(new DUUIUIMADriver.Component(
                createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, "/bundestag/",
                        XmiWriter.PARAM_PRETTY_PRINT, true,
                        XmiWriter.PARAM_OVERWRITE, true,
                        XmiWriter.PARAM_VERSION, "1.1",
                        XmiWriter.PARAM_COMPRESSION, "GZIP"
                )).withScale(1).build());

        // textimager-duui-spacy-single-de_core_news_sm
        AsyncCollectionReader testReader = new AsyncCollectionReader("C:\\Users\\CedricBorkowski\\IdeaProjects\\DockerUnifiedUIMAInterface-Fork\\src\\main\\resources\\sample", ".xmi.gz");
//        JCas cas = JCasFactory.createJCas();
//        cas.setDocumentLanguage("de");
//        cas.setDocumentText("Das ist ein Testsatz.");
        composer.run(testReader, "ReaderTest");

        composer.shutdown();
//        composer.addDriver(new DUUIRemoteDriver());
//        composer.add(new DUUIRemoteDriver.Component("http://127.0.0.1:9001"));
//        composer.add(new DUUIRemoteDriver.Component("http://127.0.0.1:9002"));
//        composer.add(new DUUIRemoteDriver.Component("http://127.0.0.1:9003"));
//        composer.add(new DUUIRemoteDriver.Component("http://127.0.0.1:9004"));
//

//
//        composer.run(cas, "Test");
//
//        for (Token token : JCasUtil.select(cas, Token.class)) {
//            POS pos = JCasUtil.selectSingleAt(cas, POS.class, token.getBegin(), token.getEnd());
//            Lemma lemma = JCasUtil.selectSingleAt(cas, Lemma.class, token.getBegin(), token.getEnd());
//            NamedEntity ner = JCasUtil.selectSingleAt(cas, NamedEntity.class, token.getBegin(), token.getEnd());
//
//            System.out.print("Text: " + token.getCoveredText() + ", ");
//            System.out.print("PoS: " + pos.getPosValue() + ", ");
//            System.out.print("Lemma: " + lemma.getValue() + ", ");
//            System.out.print("NER: " + ner.getValue());
//            System.out.println();
//        }

    }
}