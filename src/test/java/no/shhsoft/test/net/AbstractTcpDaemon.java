package no.shhsoft.test.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public abstract class AbstractTcpDaemon
extends AbstractDaemon {

    private ServerSocket serverSocket;

    private final class ClientProxy
    implements Runnable {
        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;

        public ClientProxy(final Socket socket) {
            this.socket = socket;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            final Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public void run() {
            try {
                handleCommunication(in, out);
                socket.close();
            } catch (final IOException e) {
                System.err.println("got exception: " + e.getMessage());
            }
        }
    }

    @Override
    protected final void setup() {
        try {
            serverSocket = new ServerSocket(getListenPort(), 0, null);
            /* since the accept call cannot be interrupted, we need to let the socket
             * time out periodically to be able to check if the loop should terminate.
             * not really good; should use NIO instead.  later, maybe. */
            serverSocket.setSoTimeout(500);
            serverSocket.setReuseAddress(true);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void handleCommunication(InputStream in, OutputStream out)
    throws IOException;

    @Override
    public final void run() {
        while (!isDone()) {
            try {
                new ClientProxy(serverSocket.accept());
            } catch (final SocketTimeoutException e) {
                ignore();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            serverSocket.close();
        } catch (final IOException e) {
            System.err.println("error closing socket: " + e.getMessage());
        }
    }

}
