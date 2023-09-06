import java.util.UUID;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;

public class DUUIProcess extends Thread {

  private final UUID uuid;
  private DUUIComposer composer;

  public DUUIProcess(UUID uuid, DUUIComposer composer) {
    this.uuid = UUID.randomUUID();
    this.composer = composer;
  }
}
