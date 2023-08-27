import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.xmi.XmiWriter;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;

public class Main {

  public static void main(String[] args) throws Exception {
    JCas jc = JCasFactory.createJCas();
    jc.setDocumentText("Test");
    jc.setDocumentLanguage("de");

    DUUIComposer composer = new DUUIComposer().withSkipVerification(true);

    DUUIUIMADriver uima_driver = new DUUIUIMADriver().withDebug(false);

    composer.addDriver(uima_driver);
    composer.add(
      new DUUIUIMADriver.Component(
        createEngineDescription(BreakIteratorSegmenter.class)
      )
    );

    composer.run(jc, "Test");

    JCasUtil
      .select(jc, Token.class)
      .forEach(token ->
        System.out.println(
          token.getText() + ", " + token.getBegin() + ", " + token.getEnd()
        )
      );
  }
}
