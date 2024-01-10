package api.duui.document;

public enum IOType {
    NONE("None"),
    TEXT("Text"),
    FILE("Local File"),
    DROPBOX("Dropbox"),
    MINIO("Minio");


    private final String _name;

    IOType(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }
}
