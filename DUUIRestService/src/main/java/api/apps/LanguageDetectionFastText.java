package api.apps;
import com.github.jfasttext.JFastText;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasIOUtils;
import org.xml.sax.SAXException;

public class LanguageDetectionFastText {

  private static final String modelPath = "D:\\Uni Informatik B.sc\\Bachelor\\DUUIController\\DUUIRestService\\src\\main\\resources\\lid.176.ftz";

  public static void main(String[] args) throws Exception {
    int PORT = 8000;
    HttpServer server = HttpServer.create(
      new InetSocketAddress("127.0.0.1", PORT),
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
        JFastText.ProbLabel probLabel = fasttext.predictProba(
          jc.getDocumentText()
        );
        jc.setDocumentLanguage(probLabel.label.replace("__label__", ""));
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
