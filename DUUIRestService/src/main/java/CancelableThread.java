public class CancelableThread extends Thread {
    private volatile boolean stopRequested = false;

    public void stopThread() {
        stopRequested = true;
    }

    @Override
    public void run() {
        while (!stopRequested) {
            // Your thread's logic here
            System.out.println("Thread is running...");

            // Simulate some work
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
        }

        // Clean up resources or finish any tasks before stopping
        System.out.println("Thread is stopping...");
    }

    public static void main(String[] args) throws InterruptedException {
        CancelableThread thread = new CancelableThread();
        thread.start();

        // Simulate receiving a request to stop the thread after some time
        Thread.sleep(5000);
        thread.stopThread();

        thread.join(); // Wait for the thread to finish gracefully
        System.out.println("Thread has stopped.");
    }
}