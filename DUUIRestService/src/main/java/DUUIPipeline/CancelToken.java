package DUUIPipeline;

public class CancelToken {

    private boolean isCanceled = false;

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public boolean isCanceled() {
        return isCanceled;
    }


}
