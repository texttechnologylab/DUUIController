package DUUIPipeline;


import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class DUUIPipeline {
    private String name;
    private String createdAt;
    private int timesUsed;
    private List<DUUIPipelinePart> components;

    public String getDisplayName() {
        return name;
    }

    public void setDisplayName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }

    public List<DUUIPipelinePart> getComponents() {
        return components;
    }

    public void setComponents(List<DUUIPipelinePart> components) {
        this.components = components;
    }

    public JCas construct() throws Exception {
        DUUIComposer composer = new DUUIComposer().withSkipVerification(true);
        Thread.sleep(5000);
        DUUIUIMADriver driver = new DUUIUIMADriver().withDebug(false);
        composer.addDriver(driver);
        composer.add(new DUUIUIMADriver.Component(
                createEngineDescription(BreakIteratorSegmenter.class)));


        JCas cas = JCasFactory.createJCas();
        cas.setDocumentText(FileUtils.getContentFromFile(new File("D:\\IdeaProjects\\DUUIRestService\\src\\main\\java\\testdata3.txt")));
        cas.setDocumentLanguage("de");
        composer.run(cas, name);
        composer.shutdown();
        JCasUtil.select(cas, Token.class).forEach(System.out::println);
        return cas;
    }
}
