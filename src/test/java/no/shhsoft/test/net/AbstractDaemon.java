package no.shhsoft.test.net;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public abstract class AbstractDaemon
implements Runnable {

    private Thread thread;
    private boolean done;

    protected static void ignore() {
    }

    protected final boolean isDone() {
        return done;
    }

    protected abstract void setup();

    public abstract int getListenPort();

    public final synchronized AbstractDaemon start() {
        stop();
        thread = new Thread(this);
        thread.setDaemon(true);
        setup();
        thread.start();
        for (int q = 0; q < 30; q++) {
            Thread.yield();
        }
        return this;
    }

    public final synchronized void join() {
        if (thread == null) {
            return;
        }
        try {
            thread.join();
            thread = null;
        } catch (final InterruptedException e) {
            System.err.println("interrupted waiting for thread to end.");
        }
    }

    public final synchronized void stop() {
        if (thread == null) {
            return;
        }
        done = true;
        thread.interrupt();
        join();
    }

}
