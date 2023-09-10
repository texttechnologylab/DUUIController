import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.LuaConsts;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


public class Main {
    public static void main(String[] args) throws Exception {


        DUUILuaContext context = new DUUILuaContext()
                .withGlobalLibrary("json", DUUIComposer.class.getResourceAsStream("lua_stdlib/json.lua"));
        DUUIComposer composer = new DUUIComposer().withLuaContext(context).withSkipVerification(true);
        composer.addDriver(new DUUIDockerDriver());
        composer.add(
                new DUUIDockerDriver.Component(
                        "docker.texttechnologylab.org/gnfinder:latest").withImageFetching()
        );

        // textimager-duui-spacy-single-de_core_news_sm

        JCas cas = JCasFactory.createJCas();
        cas.setDocumentLanguage("de");
        cas.setDocumentText("Das ist ein Testsatz.");
        composer.run(cas, "Test");

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