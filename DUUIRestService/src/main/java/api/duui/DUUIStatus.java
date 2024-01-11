package api.duui;

public class DUUIStatus {
    public static final String ANY = "Any";
    public static final String CANCELED = "Canceled";
    public static final String COMPLETED = "Completed";
    public static final String DECODE = "Decode";
    public static final String DESERIALIZE = "Deserialize";
    public static final String FAILED = "Failed";
    public static final String IDLE = "Idle";
    public static final String INACTIVE = "Inactive";
    public static final String INPUT = "Input";
    public static final String OUTPUT = "Output";
    public static final String RUNNING = "Running";
    public static final String SETUP = "Setup";
    public static final String SHUTDOWN = "Shutdown";
    public static final String UNKNOWN = "Unknow";
    public static final String WAITING = "Waiting";

    public static boolean oneOf(String status, String... options) {
        for (String option : options) {
            if (status.equalsIgnoreCase(option)) {
                return true;
            }
        }

        return false;
    }
}
