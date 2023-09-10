package api.process;

import java.util.concurrent.CompletableFuture;

import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;

public class DUUIProcess {

    private final String id;
    private final DUUIComposer composer;
    private final CompletableFuture<Void> thread;

    public DUUIProcess(String id, DUUIComposer composer, CompletableFuture<Void> thread) {
        this.id = id;
        this.composer = composer;
        this.thread = thread;
    }

    public CompletableFuture<Void> getThread() {
        return thread;
    }

    public DUUIComposer getComposer() {
        return composer;
    }



}

    
