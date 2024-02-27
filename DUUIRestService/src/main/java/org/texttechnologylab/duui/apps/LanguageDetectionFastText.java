package org.texttechnologylab.duui.apps;

import com.github.jfasttext.JFastText;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence_Type;
import org.apache.commons.codec.language.bm.Lang;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasIOUtils;
import org.hucompute.textimager.uima.type.Language_Type;
import org.xml.sax.SAXException;


public class LanguageDetectionFastText {

    public static class Language extends Annotation {
        public final static int typeIndexID = JCasRegistry.register(Language.class);
        public final static int type = typeIndexID;

        public Language(int addr, TOP_Type type) {
            super(addr, type);
        }

        public Language(JCas jcas) {
            super(jcas);
        }

        public Language(JCas jcas, int begin, int end) {
            super(jcas, begin, end);
        }

        public void setValue(String value) {
            this.jcasType.ll_cas.ll_setStringValue(this.addr, typeIndexID, value);
        }
    }

    private static String modelPath;

    public static void main(String[] args) throws Exception {
        modelPath = args[0];
        int PORT = 8000;
        HttpServer server = HttpServer.create(
            new InetSocketAddress("192.168.2.122", PORT),
            0
        );

        server.getAddress();
        server.createContext("/v1/communication_layer", new CommunicationLayer());
        server.createContext("/v1/typesystem", new TypesystemHandler());
        server.createContext("/v1/process", new ProcessHandler());

        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Remote Server started at " + server.getAddress());
    }

    static class ProcessHandler implements HttpHandler {

        static JCas jc;
        static JFastText fasttext;

        static {
            try {
                fasttext = new JFastText();
                fasttext.loadModel(modelPath);
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

                for (Sentence sentence : JCasUtil.select(jc, Sentence.class)) {
                    String text = sentence.getCoveredText();
                    JFastText.ProbLabel probLabel = fasttext.predictProba(text);
                    Language language = new Language(jc);
                    language.setBegin(sentence.getBegin());
                    language.setEnd(sentence.getEnd());
                    language.setValue(probLabel.label.replace("__label__", ""));
                    language.addToIndexes(jc);
                }

                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.sendResponseHeaders(200, 0);
                XmiCasSerializer.serialize(jc.getCas(), null, t.getResponseBody());
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
