package org.texttechnologylab.duui.apps.StanfordCoreNLP;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasIOUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Properties;


public class StanfordToken {


    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress("192.168.2.122", 9001), 0);

        server.getAddress();
        server.createContext("/v1/communication_layer", new StanfordToken.CommunicationLayer());
        server.createContext("/v1/typesystem", new StanfordToken.TypesystemHandler());
        server.createContext("/v1/process", new StanfordToken.ProcessHandler());

        server.setExecutor(null); // creates a default executor
        server.start();


    }

    static class ProcessHandler implements HttpHandler {

        static JCas jc;

        static {
            try {
                jc = JCasFactory.createJCas();
            } catch (UIMAException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                jc.reset();
                CasIOUtils.load(t.getRequestBody(), jc.getCas());

                Properties props = new Properties();
                props.setProperty("annotators", "tokenize");
                props.setProperty("language", jc.getDocumentLanguage());
                StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

                CoreDocument document = pipeline.processToCoreDocument(jc.getDocumentText());
                Token annotation;
                for (CoreLabel token : document.tokens()) {
                    annotation = new Token(jc, token.beginPosition(), token.endPosition());
                    annotation.setText(token.word());
                    annotation.addToIndexes(jc);
                }

                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.sendResponseHeaders(200, 0);
                XmiCasSerializer.serialize(jc.getCas(), t.getResponseBody());
                t.getResponseBody().close();

            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
            t.sendResponseHeaders(404, -1);
            t.getResponseBody().close();
        }
    }

    static class TypesystemHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                TypeSystemDescription desc = TypeSystemDescriptionFactory.createTypeSystemDescription();

                t.sendResponseHeaders(200, 0);
                OutputStream os = t.getResponseBody();
                desc.toXML(os);
                os.close();
            } catch (ResourceInitializationException e) {
                e.printStackTrace();
                t.sendResponseHeaders(404, -1);
                t.getResponseBody().close();
                return;
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
    }

    static class CommunicationLayer implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response =
                    "serial = luajava.bindClass(\"org.apache.uima.cas.impl.XmiCasSerializer\")\n" +
                            "deserial = luajava.bindClass(\"org.apache.uima.cas.impl.XmiCasDeserializer\")" +
                            "function serialize(inputCas,outputStream,params)\n" +
                            "  serial:serialize(inputCas:getCas(),outputStream)\n" +
                            "end\n" +
                            "\n" +
                            "function deserialize(inputCas,inputStream)\n" +
                            "  inputCas:reset()\n" +
                            "  deserial:deserialize(inputStream,inputCas:getCas(),true)\n" +
                            "end";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
